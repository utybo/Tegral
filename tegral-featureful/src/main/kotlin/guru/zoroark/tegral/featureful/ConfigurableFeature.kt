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

package guru.zoroark.tegral.featureful

import guru.zoroark.tegral.config.core.ConfigurationSection

/**
 * Represents a feature that provides configuration sections. These sections will be automatically parsed from Tegral
 * configuration files within the `[tegral]` section.
 */
interface ConfigurableFeature<T> : Feature<T> {
    /**
     * Returns the sections that this feature provides for the `[tegral]` section.
     */
    val configurationSections: List<ConfigurationSection<*>>
}
