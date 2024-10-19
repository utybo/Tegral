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

package guru.zoroark.tegral.web.controllers

import io.ktor.server.application.*
import io.ktor.server.engine.*
import java.io.File

/**
 * Application settings for the Ktor application. Identical to the parameters of the [embeddedServer] function from
 * Ktor.
 */
class KtorApplicationSettings<TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>(
    /**
     * The engine to use for the Ktor application.
     *
     * See [Ktor's documentation](https://ktor.io/docs/engines.html) for more information.
     */
    val engine: ApplicationEngineFactory<TEngine, TConfiguration>,
    /**
     * The port on which to listen for HTTP connections. `80` by default.
     */
    val port: Int = 80,
    /**
     * The host on which to listen for HTTP connections. `0.0.0.0` by default.
     */
    val host: String = "0.0.0.0",
    /**
     * Paths which are listened to for changes to reload classes in dev mode.
     *
     * See [Ktor's documentation](https://ktor.io/docs/auto-reload.html#watch-paths) for more information.
     */
    val watchPaths: List<String> = listOf(File(".").canonicalPath),
    private val configure: TConfiguration.() -> Unit = {}
) {
    /**
     * Equivalent to `embeddedServer` but uses this object's information as the parameters.
     */
    fun embeddedServerFromSettings(block: Application.() -> Unit) =
        embeddedServer(
            engine,
            serverConfig {
                watchPaths = this@KtorApplicationSettings.watchPaths
                module(block)
            },
        ) {
            connector {
                this.port = this@KtorApplicationSettings.port
                this.host = this@KtorApplicationSettings.host
            }
            configure()
        }
}
