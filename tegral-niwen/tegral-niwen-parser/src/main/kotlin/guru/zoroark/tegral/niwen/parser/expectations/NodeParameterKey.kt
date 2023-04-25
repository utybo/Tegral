/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package guru.zoroark.tegral.niwen.parser.expectations

import guru.zoroark.tegral.core.TegralDsl
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * A key used when storing state within Niwen parsers.
 *
 * State storage in Niwen parsers is based on a map with keys that are instances of this class. These keys are,
 * fundamentally, a type-contextualized typed name, i.e.:
 *
 * - Some name
 * - A type `typeOf<R>()` for that name
 * - A contextual type `T` that represents a type on which it's possible to "key" something that is named and typed
 * according to the two above points.
 *
 * For example, a key for the "bar" property of the following class:
 *
 * ```kotlin
 * data class Foo(val bar: String) {
 *     companion object : /* ... */
 * }
 * ```
 *
 * ... would be `NodeParameterKey<Foo, String>(typeOf<String>(), "bar")`.
 *
 * ### Creating keys
 *
 * You will generally not need to manually create keys within the Niwen parser DSL. You can create a key:
 *
 * - from a `KProperty1` using [asKey], e.g. `Foo::bar.asKey()`
 * - with a function call using [key]
 *
 *
 * ### Key variance
 *
 * Key (pun intended) to understanding the variance of R in this type is how the actual output type (from the
 * perspective of the node) is used.
 *
 * Imagine the following parser setup:
 *
 * ```
 * open class Food
 * class Strawberry
 *
 * either<CharSequence> { <<----------------------------+
 *     // This produces a String                        | Used as input of
 *     expect(someToken) storeIn <result of either> >>--+
 * }
 * ```
 *
 * In plain English, `expect(someToken)` exposes a string that is stored, and therefore expects a `NodeParameterKey<...,
 * String>`. However, we could well say "actually, I want to store the String in a CharSequence", like in the example
 * above. This would be equivalent to passing a String value to a function that expects a CharSequence, which is valid.
 * This is why, despite being an "output" (as in, the type of the value that the key indexes), it is an **input**
 * variance, because it can (and will) be used as an **input** for storage purposes.
 *
 * (Note, the example above is heavily simplified. The real version would use the `self()` and `by subtype()`
 * mechanisms).
 */
data class NodeParameterKey<in T, in R>(
    /**
     * Output type for this key. Should correspond to `typeOf<R>()`.
     */
    val outputType: KType,
    /**
     * Name of this key
     */
    val name: String
) {
    override fun toString(): String = "$name ($outputType)"
}

/**
 * An escape hatch for [NodeParameterKey]'s harsh typing restrictions.
 *
 * Raw keys can be used with `storeIn` just like [NodeParameterKey]s, but are less safe and should only be used as a
 * last resort.
 */
data class RawKey(
    /**
     * Output type for this key. Should correspond to `typeOf<R>()`.
     */
    val outputType: KType,
    /**
     * Name of this key
     */
    val name: String
)

/**
 * Create a [NodeParameterKey] from the provided name and types.
 *
 * See [NodeParameterKey] for more information on the type parameters.
 */
@TegralDsl
inline fun <T, reified R> key(name: String): NodeParameterKey<T, R> {
    return NodeParameterKey(typeOf<R>(), name)
}

/**
 * Create a [NodeParameterKey] for the provided property.
 */
fun <T, R> KProperty1<T, R>.asKey(): NodeParameterKey<T, R> {
    return NodeParameterKey(this.returnType, this.name)
}
