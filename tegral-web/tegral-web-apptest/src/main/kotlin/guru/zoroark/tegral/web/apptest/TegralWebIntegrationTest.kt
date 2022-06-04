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

import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.ExtensibleEnvironmentContextBuilderDsl
import guru.zoroark.tegral.di.services.services
import guru.zoroark.tegral.di.services.useServices
import guru.zoroark.tegral.di.test.TegralDiBaseTest
import guru.zoroark.tegral.di.test.TestMutableInjectionEnvironment
import guru.zoroark.tegral.di.test.UnsafeMutableEnvironment
import guru.zoroark.tegral.services.feature.ServicesFeature
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.server.testing.ClientProvider
import kotlinx.coroutines.runBlocking

interface MultiClientProvider : ClientProvider {
    fun client(appName: String?): HttpClient
    fun createClient(appName: String?, block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit): HttpClient

    override val client: HttpClient get() = client(null)
    override fun createClient(block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit): HttpClient =
        createClient(null, block)
}

class WebIntegrationTestEnvironment(
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

interface TestWebApplicationDsl : ExtensibleContextBuilderDsl {
    fun install(testFeature: IntegrationTestFeature)
}

class TestWebApplicationBuilder : TestWebApplicationDsl {
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

    fun build(): WebIntegrationTestEnvironment {
        for (feature in features) {
            with(feature) { install() }
        }
        val env = UnsafeMutableEnvironment(envBuilder.build())
        return WebIntegrationTestEnvironment(env)
    }
}

abstract class TegralWebIntegrationTest(
    private val baseSetup: TestWebApplicationDsl.() -> Unit
) : TegralDiBaseTest<WebIntegrationTestEnvironment>() {
    open fun TestWebApplicationDsl.setupDefaults() {
        install(TestServicesFeature)
        install(KtorTestApplicationFeature)
    }

    override fun <T> test(
        additionalBuilder: ContextBuilderDsl.() -> Unit,
        block: suspend WebIntegrationTestEnvironment.() -> T
    ): T {
        val builder = TestWebApplicationBuilder()
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

