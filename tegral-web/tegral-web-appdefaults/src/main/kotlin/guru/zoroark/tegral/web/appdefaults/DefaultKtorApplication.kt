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

package guru.zoroark.tegral.web.appdefaults

import guru.zoroark.tegral.config.core.TegralConfig
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.web.config.WebConfiguration
import guru.zoroark.tegral.web.controllers.KtorApplication
import guru.zoroark.tegral.web.controllers.KtorApplicationSettings
import io.ktor.server.netty.Netty

/**
 * A basic implementation of [KtorApplication] that uses sane defaults.
 *
 * This implementation sets up the following:
 *
 * - Adds the ContentNegotiation feature to the application with Jackson (and sets up the JavaTimeModule on Jackson)
 * - Uses Netty as the server backend
 * - Uses the [WebConfiguration] to configure the host and port of the application
 */
open class DefaultKtorApplication(scope: InjectionScope) : KtorApplication(scope) {
    private val tegralConfig: TegralConfig by scope()

    override val settings
        get() = KtorApplicationSettings(
            Netty,
            port = tegralConfig[WebConfiguration].port,
            host = tegralConfig[WebConfiguration].host
        )
}
