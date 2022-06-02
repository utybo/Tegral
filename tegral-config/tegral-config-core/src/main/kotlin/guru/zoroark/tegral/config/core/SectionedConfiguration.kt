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

typealias ConfigurationSections = Map<ConfigurationSection<*>, Any>

/**
 * A class that can contain an arbitrary number of "sections".
 *
 * This allows to have a dynamic(ish) class inside a data class that gets loaded by Hoplite. This class can contain any
 * number of sections. These sections are registered in and loaded by a [SectionedConfigurationDecoder].
 */
open class SectionedConfiguration(
    /**
     * The sections available in this configuration. Consider using the [get] operator instead of directly using this
     * map.
     */
    val sections: ConfigurationSections
) {
    /**
     * Retrieves a configuration via its section, or throws an exception if no such section could be found.
     */
    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(section: ConfigurationSection<T>): T = sections.getOrElse(section) {
        throw UnknownSectionException(section.name)
    } as T

    override fun toString(): String {
        return buildString {
            append(this@SectionedConfiguration::class.simpleName ?: "SectionedConfiguration")
            append("(")
            sections.map { (section, value) -> "${section.name}=$value" }.joinTo(this, ", ")
            append(")")
        }
    }
}
