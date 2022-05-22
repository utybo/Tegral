package guru.zoroark.tegral.config.core

import kotlin.reflect.KClass

open class ConfigurationSection<T : Any>(
    val name: String,
    val isOptional: SectionOptionality<T>,
    val kclass: KClass<T>
)

sealed class SectionOptionality<out T> {
    data class Optional<out T>(val defaultValue: T) : SectionOptionality<T>()
    object Required : SectionOptionality<Nothing>()
}
