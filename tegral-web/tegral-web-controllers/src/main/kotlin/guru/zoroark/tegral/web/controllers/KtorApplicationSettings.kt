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

import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.engine.embeddedServer
import java.io.File

/**
 * Application settings for the Ktor application. Identical to the parameters of the [embeddedServer] function from
 * Ktor.
 */
class KtorApplicationSettings<TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>(
    val engine: ApplicationEngineFactory<TEngine, TConfiguration>,
    val port: Int = 80,
    val host: String = "0.0.0.0",
    val watchPaths: List<String> = listOf(File(".").canonicalPath),
    private val configure: TConfiguration.() -> Unit = {}
) {
    /**
     * Equivalent to `embeddedServer` but uses this object's information as the parameters.
     */
    fun embeddedServerFromSettings(block: Application.() -> Unit) =
        embeddedServer(engine, port, host, watchPaths, configure, block)
}
