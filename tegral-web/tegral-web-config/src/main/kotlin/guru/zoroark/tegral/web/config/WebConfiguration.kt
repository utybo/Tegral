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

package guru.zoroark.tegral.web.config

import guru.zoroark.tegral.config.core.ConfigurationSection
import guru.zoroark.tegral.config.core.SectionOptionality

/**
 * Configuration section used in the `[tegral.web]` section of the configuration file.
 */
data class WebConfiguration(
    /**
     * The port to use for hosting the main application.
     */
    val port: Int,
    /**
     * The hostname the main application will be bound to. `0.0.0.0` will be used by default and binds to all
     * network interfaces.
     */
    val host: String = "0.0.0.0"
) {
    /**
     * The [ConfigurationSection] for [WebConfiguration].
     */
    companion object :
        ConfigurationSection<WebConfiguration>("web", SectionOptionality.Required, WebConfiguration::class)
}
