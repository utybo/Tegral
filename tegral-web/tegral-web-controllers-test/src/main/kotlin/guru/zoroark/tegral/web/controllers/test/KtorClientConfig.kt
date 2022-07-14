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

import guru.zoroark.tegral.web.appdefaults.DefaultAppDefaultsModule
import guru.zoroark.tegral.web.appdefaults.defaultTegralConfiguration
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson

/**
 * Applies AppDefaults-compatible configuration for clients used in test classes.
 *
 * This is the "client-side counterpart" of AppDefaults' [DefaultAppDefaultsModule]
 */
fun KtorTestClientConfig.applyDefaultsModule() {
    install(ContentNegotiation) {
        jackson { defaultTegralConfiguration() }
    }
}
