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

import guru.zoroark.tegral.web.controllers.KtorApplication
import guru.zoroark.tegral.web.controllers.KtorModule
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

/**
 * Implementation of [DefaultsModule] for the default "primary" [KtorApplication] (i.e. the application with name
 * `null`)
 *
 * This is added to the environment by default when using the [AppDefaultsFeature].
 */
class DefaultAppDefaultsModule : DefaultsModule(null)

/**
 * Abstract module that applies the AppDefaults configuration for Ktor applications.
 *
 * Typical usage should be done like [DefaultAppDefaultsModule]: simply subclass this class and supply your
 * application's name
 */
abstract class DefaultsModule(appName: String?) : KtorModule(DEFAULT_APP_SETUP_MODULE_PRIORITY, appName) {
    override fun Application.install() {
        install(ContentNegotiation) {
            jackson {
                defaultTegralConfiguration()
            }
        }
    }
}
