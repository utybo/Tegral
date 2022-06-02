package guru.zoroark.tegral.di.environment

import kotlin.reflect.KClass

/**
 * Identifies an injectable component via its type and optionally via other elements called [qualifiers][Qualifier].
 *
 * By default, identifiers use the [empty qualifier object][EmptyQualifier] as a way of saying "there is no qualifier
 * here". You generally do not need to use qualifiers if your environment only contains at most one object of a specific
 * type. If you do need multiple objects of the same type, qualifiers such as the [NameQualifier] should be used to
 * differentiate them.
 *
 * @property kclass The class this identifier wraps
 * @property qualifier The qualifier for this identifier.
 */
data class Identifier<T : Any>(val kclass: KClass<T>, val qualifier: Qualifier = EmptyQualifier) {
    override fun toString(): String {
        return (kclass.qualifiedName ?: "<anonymous>") + " ($qualifier)"
    }
}
