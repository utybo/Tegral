/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package guru.zoroark.tegral.web.controllers.test

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.test.TegralAbstractSubjectTest
import guru.zoroark.tegral.di.test.TegralSubjectTest
import guru.zoroark.tegral.di.test.TestMutableInjectionEnvironment
import guru.zoroark.tegral.di.test.UnsafeMutableEnvironment
import guru.zoroark.tegral.web.appdefaults.DefaultAppDefaultsModule
import guru.zoroark.tegral.web.controllers.KtorExtension
import guru.zoroark.tegral.web.controllers.KtorModule
import guru.zoroark.tegral.web.controllers.filterIsKclassSubclassOf
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.ClientProvider
import io.ktor.server.testing.TestApplicationBuilder
import io.ktor.server.testing.testApplication
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

typealias KtorTestClientConfig = HttpClientConfig<out HttpClientEngineConfig>

/**
 * A [subject-based][TegralAbstractSubjectTest] test for Controllers-based classes (more specifically, for subclasses of
 * [KtorModule] and [KtorController]).
 *
 * The constructors follow the same pattern as [TegralSubjectTest]
 */
// TODO document that it is appname insensitive
abstract class TegralControllerTest<TSubject : Any>(
    subjectClass: KClass<TSubject>,
    private val baseModule: InjectableModule
) : TegralAbstractSubjectTest<TSubject, ControllerTestContext>(subjectClass) {

    constructor(subjectClass: KClass<TSubject>, baseModuleBuilder: ContextBuilderDsl.() -> Unit) : this(
        subjectClass,
        tegralDiModule("<base test module>", baseModuleBuilder)
    )

    @Suppress("UNCHECKED_CAST")
    constructor(constructor: KFunction<TSubject>) : this(
        constructor.returnType.jvmErasure as KClass<TSubject>,
        { put(constructor.returnType.jvmErasure as KClass<TSubject>, constructor) }
    )

    /**
     * Applies default values for the application.
     *
     * By default, this just puts [DefaultAppDefaultsModule] in the environment to apply application defaults from
     * AppDefaults. Override this function if you wish to add something else.
     */
    protected open fun ExtensibleContextBuilderDsl.applyDefaultsModule() {
        put(::DefaultAppDefaultsModule)
    }

    /**
     * A default configuration for clients you can retrieve while running tests.
     *
     * By default, this applies sane defaults for interacting with AppDefaults-powered applications. You can override
     * this function to change its behavior.
     */
    protected open fun KtorTestClientConfig.configureClient() {
        applyDefaultsModule()
    }

    @TegralDsl
    override fun <T> test(
        additionalBuilder: ContextBuilderDsl.() -> Unit,
        block: suspend ControllerTestContext.() -> T
    ): T {
        // Very dirty, but testApplication is not inline and does not have a contract :(
        val resultRef = mutableListOf<T>()
        // Initialize base test environment
        val env = tegralDi(UnsafeMutableEnvironment, UnsafeMutableEnvironment.Meta) {
            meta { put { KtorExtension(scope, true) } }
            applyDefaultsModule()
            put(baseModule)
            additionalBuilder()
        }
        val modules = env.metaEnvironment.get<KtorExtension>().getAllModules()
        testApplication {
            application { modules.forEach { with(it) { install() } } }
            env.put { this@testApplication }
            val context = DefaultControllerTestContext(this@testApplication, env) { configureClient() }
            resultRef += context.block()
        }
        return resultRef.singleOrNull()
            ?: error("Internal error: resultRef does not contain exactly one result. Please report this.")
    }
}

/**
 * The context provided to applications in [TegralControllerTest.test]'s block.
 *
 * Three kinds of elements are available:
 *
 * - Elements from the Tegral DI test environment (provided via [TestMutableInjectionEnvironment])
 * - The test Ktor clients (provided via [ClientProvider])
 * - Additional configuration from the [TestApplicationBuilder] (provided via [applicationBuilder])
 */
interface ControllerTestContext : TestMutableInjectionEnvironment, ClientProvider {
    /**
     * Provides this test's [TestApplicationBuilder] in the lambda, which can be used to further set up Ktor's test
     * facilities.
     */
    fun applicationBuilder(block: TestApplicationBuilder.() -> Unit)
}

/**
 * A default implementation of [ControllerTestContext] that delegates implementations to existing elements.
 */
class DefaultControllerTestContext(
    private val appBuilder: ApplicationTestBuilder,
    private val environment: TestMutableInjectionEnvironment,
    private val configureClient: KtorTestClientConfig.() -> Unit
) : ControllerTestContext, TestMutableInjectionEnvironment by environment, ClientProvider {
    override fun applicationBuilder(block: TestApplicationBuilder.() -> Unit) {
        appBuilder.block()
    }

    override val client: HttpClient by lazy { createClient { } }

    override fun createClient(block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit): HttpClient =
        appBuilder.createClient {
            this@DefaultControllerTestContext.configureClient(this)
            block()
        }
}
