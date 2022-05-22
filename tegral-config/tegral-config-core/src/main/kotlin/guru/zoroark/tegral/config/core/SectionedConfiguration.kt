package guru.zoroark.tegral.config.core

open class SectionedConfiguration(
    val sections: Map<ConfigurationSection<*>, Any>
) {
    operator fun <T : Any> get(section: ConfigurationSection<T>): T = sections[section] as T
}
