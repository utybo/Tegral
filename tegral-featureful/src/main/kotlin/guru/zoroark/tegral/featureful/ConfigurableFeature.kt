package guru.zoroark.tegral.featureful

import guru.zoroark.tegral.config.core.ConfigurationSection

interface ConfigurableFeature : Feature {
    val configurationSections: List<ConfigurationSection<*>>
}
