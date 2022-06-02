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
import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.test.TegralAbstractSubjectTest
import guru.zoroark.tegral.di.test.TestMutableInjectionEnvironment
import guru.zoroark.tegral.di.test.UnsafeMutableEnvironment
import guru.zoroark.tegral.web.controllers.KtorModule
import guru.zoroark.tegral.web.controllers.filterIsKclassSubclassOf
import guru.zoroark.tegral.web.controllers.getKtorModulesByPriority
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.ClientProvider
import io.ktor.server.testing.TestApplicationBuilder
import io.ktor.server.testing.testApplication
import kotlin.reflect.KClass

/**
 * A [subject-based][TegralAbstractSubjectTest] test for Controllers-based classes (more specifically, for subclasses of
 * [KtorModule] and [KtorController]).
 */
abstract class TegralControllerTest<TSubject : Any>(
    subjectClass: KClass<TSubject>,
    private val baseModule: InjectableModule,
    private val appName: String? = null
) : TegralAbstractSubjectTest<TSubject, ControllerTestContext>(subjectClass) {

    @TegralDsl
    override fun <T> test(
        additionalBuilder: ContextBuilderDsl.() -> Unit,
        block: suspend ControllerTestContext.() -> T
    ): T {
        // Very dirty, but testApplication is not inline and does not have a contract :(
        val resultRef = mutableListOf<T>()
        // Initialize base test environment
        val env = tegralDi(UnsafeMutableEnvironment) {
            put(baseModule)
            additionalBuilder()
        }
        // Initialize application client
        val modules =
            env.getKtorModulesByPriority(
                env.components.keys.asSequence().filterIsKclassSubclassOf<KtorModule>(),
                appName
            )
        testApplication {
            application { modules.forEach { with(it) { install() } } }
            env.put { this@testApplication }
            val context = DefaultControllerTestContext(this@testApplication, env)
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
    private val environment: TestMutableInjectionEnvironment
) : ControllerTestContext, TestMutableInjectionEnvironment by environment, ClientProvider by appBuilder {
    override fun applicationBuilder(block: TestApplicationBuilder.() -> Unit) {
        appBuilder.block()
    }
}
