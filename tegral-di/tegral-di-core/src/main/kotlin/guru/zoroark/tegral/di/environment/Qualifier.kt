package guru.zoroark.tegral.di.environment

/**
 * Qualifiers are simple objects that can be used within [Identifier] objects to provide additional differentiators
 * between two same-type objects or components.
 *
 * There are two built-in kinds of qualifiers:
 *
 * - The [EmptyQualifier], which is the type when no qualifier is used.
 * - The [NameQualifier], which is based on String objects.
 */
interface Qualifier {
    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}

/**
 * The empty qualifier object must be used when there is no qualifier being used.
 *
 * This is the default qualifier object in all DSL functions if you do not specify any.
 */
object EmptyQualifier : Qualifier {
    override fun toString() =
        "<no qualifier>"

    override fun equals(other: Any?) =
        other === EmptyQualifier

    override fun hashCode(): Int = 1
}
