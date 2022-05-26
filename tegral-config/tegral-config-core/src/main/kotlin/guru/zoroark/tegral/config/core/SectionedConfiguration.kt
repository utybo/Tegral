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
}
