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
