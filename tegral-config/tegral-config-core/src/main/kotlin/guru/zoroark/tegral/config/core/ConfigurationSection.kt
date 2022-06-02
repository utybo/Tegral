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

package guru.zoroark.tegral.config.core

import kotlin.reflect.KClass

/**
 * A configuration section that can be added to a [SectionedConfiguration].
 *
 * Note that this is metadata to provide a bridge between a [SectionedConfiguration] and your data class.
 */
open class ConfigurationSection<T : Any>(
    /**
     * The name of this section. This is the name that is used in the config file.
     */
    val name: String,
    /**
     * Information on the optionality of this section.
     *
     * @see SectionOptionality
     */
    val isOptional: SectionOptionality<T>,
    /**
     * The KClass that corresponds to [T].
     */
    val kclass: KClass<T>
)

/**
 * Provides information on whether a section is optional or not.
 */
sealed class SectionOptionality<out T> {
    /**
     * The section is optional. If absent, [defaultValue] is used instead.
     */
    data class Optional<out T>(
        /**
         * The default value to use if the section is absent.
         */
        val defaultValue: T
    ) : SectionOptionality<T>()

    /**
     * The section is required. An exception will be thrown if it is absent from the configuration file.
     */
    object Required : SectionOptionality<Nothing>()
}
