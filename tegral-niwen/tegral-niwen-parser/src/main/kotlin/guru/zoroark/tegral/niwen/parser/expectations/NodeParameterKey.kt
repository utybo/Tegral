package guru.zoroark.tegral.niwen.parser.expectations

import guru.zoroark.tegral.core.TegralDsl
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.typeOf

// NodeParameterKey<in T, in R>()
// NodeParameterKey<..., CharSequence>(typeOf<CharSequence>())
//           ^
//           |
// NodeParameterKey<..., String>(typeOf<CharSequence>())

/*
 * Key to understanding the variance of R in this type is how the actual output type (from the perspective of the node)
 * is used.
 *
 * Imagine the following parser setup:
 *
 * ```
 * open class Food
 * class Strawberry
 *
 * either<CharSequence> { <<----------------------------+
 *     // This produces a String                        | Used as input of
 *     expect(someToken) storeIn <result of either> ----+
 * }
 * ```
 *
 * In plain English, `expect(someToken)` exposes a string that is stored, and therefore expects a
 * NodeParameterKey<..., String>. However, we could well say "actually, I want to store the String in a CharSequence",
 * like in the example above. This would be equivalent to passing a String value to a function that expects a
 * CharSequence, which is valid. This is why, despite being an "output" (as in, the type of the value that the key
 * indexes), it is an **input** variance, because it can (and will) be used as an **input** for storage purposes.
 *
 * (Note, the example above is heavily simplified. The real version would use the `self()` and `by subtype()` mechanisms).
 */
data class NodeParameterKey<in T, in R>(val outputType: KType, val name: String) {
    override fun toString(): String = "$name ($outputType)"
}

data class RawKey<R>(val outputType: KType, val name: String)

@TegralDsl
inline fun <T, reified R> key(name: String): NodeParameterKey<T, R> {
    return NodeParameterKey(typeOf<R>(), name)
}

fun <T, R> KProperty1<T, R>.asKey(): NodeParameterKey<T, R> {
    return NodeParameterKey(this.returnType, this.name)
}
