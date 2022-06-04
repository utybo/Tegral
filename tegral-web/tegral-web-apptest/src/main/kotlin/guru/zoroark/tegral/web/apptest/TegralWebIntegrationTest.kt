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

package guru.zoroark.tegral.web.apptest

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.ExtensibleEnvironmentContextBuilderDsl
import guru.zoroark.tegral.di.services.services
import guru.zoroark.tegral.di.test.TegralDiBaseTest
import guru.zoroark.tegral.di.test.TestMutableInjectionEnvironment
import guru.zoroark.tegral.di.test.UnsafeMutableEnvironment
import guru.zoroark.tegral.web.controllers.test.TegralControllerTest
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import kotlinx.coroutines.runBlocking

/**
 * DSL provided for building integration test environments.
 */
interface WebIntegrationTestContextDsl : ExtensibleContextBuilderDsl {
    /**
     * Adds the given integration test feature to the environment.
     */
    fun install(testFeature: IntegrationTestFeature)
}

/**
 * Builder for integration test environments.
 *
 * Comparable to a regular Tegral DI environment builder, with the addition of installable features.
 */
class WebIntegrationTestContextBuilder : WebIntegrationTestContextDsl {
    private val envBuilder = ExtensibleEnvironmentContextBuilderDsl()
    private val features = mutableListOf<IntegrationTestFeature>()

    override fun <T : Any> put(declaration: Declaration<T>) {
        envBuilder.put(declaration)
    }

    override fun install(testFeature: IntegrationTestFeature) {
        features += testFeature
    }

    override fun meta(action: ContextBuilderDsl.() -> Unit) {
        envBuilder.meta(action)
    }

    /**
     * Build a [WebIntegrationTestContext], installing required features, constructing an environment, etc.
     */
    fun build(): WebIntegrationTestContext {
        for (feature in features) {
            with(feature) { install() }
        }
        val env = UnsafeMutableEnvironment(envBuilder.build())
        return WebIntegrationTestContext(env)
    }
}

/**
 * Context within which integration tests are run.
 *
 * This is similar to any regular Tegral DI test environment (as in, you can get, put elements, etc.), with the addition
 * of the ability to retrieve HTTP clients to send requests to Ktor applications present in the environment.
 */
class WebIntegrationTestContext(
    private val env: UnsafeMutableEnvironment
) : TestMutableInjectionEnvironment by env, MultiClientProvider {
    private fun findTestApp(appName: String?): KtorTestApplication {
        return env.components.values.asSequence()
            .filterIsInstance<KtorTestApplication>()
            .firstOrNull { it.appName == appName }
            ?: error("Test application not found $appName") // TODO add instructions for adding separate app
    }

    override fun client(appName: String?): HttpClient = findTestApp(appName).client

    override fun createClient(
        appName: String?,
        block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit
    ): HttpClient {
        return findTestApp(appName).createClient(block)
    }
}

/**
 * Test class for integration tests.
 *
 * Integration tests provide a lightweight [feature-based][IntegrationTestFeature] environment to run (portions of)
 * applications.
 *
 * This is comparable to a [TegralControllerTest] on steroids.
 */
abstract class TegralWebIntegrationTest(
    private val baseSetup: WebIntegrationTestContextDsl.() -> Unit
) : TegralDiBaseTest<WebIntegrationTestContext>() {
    /**
     * Installs default features (most notably [TestServicesFeature] and [KtorTestApplicationFeature]). Can be
     * overridden if you want to change the defaults -- otherwise, adding components should be done via the [baseSetup]
     * parameter in the [TegralWebIntegrationTest] constructor.
     */
    open fun WebIntegrationTestContextDsl.setupDefaults() {
        install(TestServicesFeature)
        install(KtorTestApplicationFeature)
    }

    @TegralDsl
    override fun <T> test(
        additionalBuilder: ContextBuilderDsl.() -> Unit,
        block: suspend WebIntegrationTestContext.() -> T
    ): T {
        val builder = WebIntegrationTestContextBuilder()
        builder.apply { setupDefaults() }
        builder.apply(baseSetup)
        builder.apply(additionalBuilder)
        val env = builder.build()
        return runBlocking {
            env.services.startAll()
            val result = env.block()
            env.services.stopAll()
            result
        }
    }
}
