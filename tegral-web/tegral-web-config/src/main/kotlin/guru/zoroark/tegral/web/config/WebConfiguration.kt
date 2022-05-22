package guru.zoroark.tegral.web.config

import guru.zoroark.tegral.config.core.ConfigurationSection
import guru.zoroark.tegral.config.core.SectionOptionality

data class WebConfiguration(
    val port: Int,
    val host: String = "0.0.0.0"
) {
    companion object :
        ConfigurationSection<WebConfiguration>("web", SectionOptionality.Required, WebConfiguration::class)
}
