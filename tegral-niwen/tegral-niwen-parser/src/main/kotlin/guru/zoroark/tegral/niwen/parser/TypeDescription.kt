package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey

/**
 * The description for the creation of a type
 */
class TypeDescription<T>(
    /**
     * The arguments: that is, everything that was expected and stored using
     * the `storeIn`/`storeValueIn` constructs.
     */
    val arguments: Map<NodeParameterKey<T, *>, *>
) {
    /**
     * Retrieve the given argument, casting it to `T` automatically
     */
    inline operator fun <reified R> get(parameterKey: NodeParameterKey<T, R>): R {
        val value = arguments.getOrElse(parameterKey) {
            throw NiwenParserException("Key '$parameterKey' does not exist in the stored arguments")
        }
        if (value is R) {
            return value // Auto-cast by Kotlin
        } else {
            throw NiwenParserException("Expected $parameterKey to be of type ${R::class}, but it is actually of type ${value?.let { it::class }}")
        }
    }
}