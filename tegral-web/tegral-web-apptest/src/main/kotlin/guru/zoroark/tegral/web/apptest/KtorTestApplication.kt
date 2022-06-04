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

import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.services.api.TegralService
import guru.zoroark.tegral.web.controllers.KtorApplication
import guru.zoroark.tegral.web.controllers.KtorExtension
import guru.zoroark.tegral.web.controllers.test.KtorTestClientConfig
import guru.zoroark.tegral.web.controllers.test.applyDefaultsModule
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.ClientProvider

/**
 * A component that manages [KtorApplication] components and provides test facilities over them.
 *
 * **Note: in a test environment, you must add `with noService` to your KtorApplication declaration, like so:**
 *
 * ```kotlin
 * put(::MyKtorApplication) with noService
 * ```
 *
 * This is because integration testing environments perform `start` sequences like regular environments, yet we do not
 * generally want to *actually* start a Ktor server.
 */
abstract class KtorTestApplication(
    scope: InjectionScope,
    /**
     * The name of the [KtorApplication] this instance is for.
     */
    val appName: String?
) : TegralService, ClientProvider {
    private val ktorExtension: KtorExtension by scope.meta()

    private val applicationTestBuilder = ApplicationTestBuilder()

    /**
     * A function called for every created client for the application.
     *
     * You can use this to apply required presets. For example, [DefaultKtorTestApplication] uses this to call
     * [applyDefaultsModule] which applies AppDefaults settings to make the client work.
     */
    open fun KtorTestClientConfig.configureClient() {
    }

    override suspend fun start() {
        val modules = ktorExtension.getModulesForAppName(appName)
        modules.forEach { module ->
            applicationTestBuilder.application {
                with(module) { install() }
            }
        }
    }

    override suspend fun stop() {
        // No-op
    }

    override fun createClient(block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit): HttpClient =
        applicationTestBuilder.createClient {
            val clientConfig = this
            with(this@KtorTestApplication) { clientConfig.configureClient() }
            block()
        }

    override val client: HttpClient by lazy { createClient {} }
}

/**
 * Implementation of [KtorTestApplication] for the `null` app name that applies
 * [defaults for the Ktor client][applyDefaultsModule].
 */
open class DefaultKtorTestApplication(scope: InjectionScope) : KtorTestApplication(scope, null) {
    override fun KtorTestClientConfig.configureClient() {
        applyDefaultsModule()
    }
}
