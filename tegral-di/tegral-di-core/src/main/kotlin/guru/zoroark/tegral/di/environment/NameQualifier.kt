package guru.zoroark.tegral.di.environment

import guru.zoroark.tegral.core.TegralDsl

/**
 * A qualifier that is based on a string. You can also use [named] to construct name qualifiers in a more DSL-ish
 * approach.
 *
 * @property name The name for this qualifier.
 */
data class NameQualifier(val name: String) : Qualifier

/**
 * Creates a [NameQualifier] with the given name.
 */
@TegralDsl
fun named(name: String) = NameQualifier(name)
