package guru.zoroark.tegral.featureful

import guru.zoroark.tegral.config.core.ConfigurationSection

/**
 * Represents a feature that provides configuration sections. These sections will be automatically parsed from Tegral
 * configuration files within the `[tegral]` section.
 */
interface ConfigurableFeature : Feature {
    /**
     * Returns the sections that this feature provides for the `[tegral]` section.
     */
    val configurationSections: List<ConfigurationSection<*>>
}
