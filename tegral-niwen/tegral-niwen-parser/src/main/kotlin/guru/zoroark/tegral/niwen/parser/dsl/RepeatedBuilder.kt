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

package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedRepeated
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.RepeatedItemReceiver
import guru.zoroark.tegral.niwen.parser.expectations.StateCallback
import kotlin.reflect.typeOf

/**
 * Builder class for the content of a [repeated] block
 */
class ItemExpectationBuilder<R> :
    ExpectationReceiver<RepeatedItemReceiver<R>>, Buildable<List<Expectation<RepeatedItemReceiver<R>, *>>> {
    private val expectations = mutableListOf<Buildable<Expectation<RepeatedItemReceiver<R>, *>>>()
    override fun plusAssign(expectationBuilder: Buildable<Expectation<RepeatedItemReceiver<R>, *>>) {
        expectations += expectationBuilder
    }

    override fun build(): List<Expectation<RepeatedItemReceiver<R>, *>> {
        return expectations.map { it.build() }
    }
}

/**
 * Builder class for [ExpectedRepeated].
 */
class RepeatedBuilder<T, R>(
    private val itemExpectationBuilder: ItemExpectationBuilder<R>,
    private val callback: StateCallback<T, List<R>, *>?
) : Buildable<ExpectedRepeated<T, R>> {
    override fun build(): ExpectedRepeated<T, R> {
        return ExpectedRepeated(itemExpectationBuilder.build(), callback)
    }
}

/**
 * Used with `storeIn`, allows placing some state as the item of the output list.
 */
@TegralDsl
inline val <reified R> ItemExpectationBuilder<R>.item
    get() = NodeParameterKey<RepeatedItemReceiver<R>, R>(typeOf<R>(), "0")

// TODO Whenever K2 lands, remove the "Type inference failure" section
/**
 * Repeatedly runs the provided expectations until they no longer match, then collects the [item]s (specified via
 * `storeIn item` in one of the expectations in the [repeated] block) into a list that you can then store.
 *
 * Usage can look like this:
 *
 * ```kotlin
 * data class ManyWords(val words: List<String>)
 *
 * val parser = niwenParser<ManyWords> {
 *     ManyWords root {
 *         repeated {
 *             expect(Word) storeIn item
 *         } storeIn ManyWords::words
 *     }
 * }
 * ```
 *
 * ### Type inference failure
 *
 * For whatever reason, when using the legacy Kotlin compiler (i.e. not K2), builder inference can sometimes fail when
 * using `repeated`. In this case, manually provide type parameters, either where you use `repeated` or on the
 * `niwenParser` root call.
 *
 * The ultimate fix is to wait for when K2 is released.
 */
@TegralDsl
fun <T, R> ExpectationReceiver<T>.repeated(
    itemBuilder: ItemExpectationBuilder<R>.() -> Unit
): ExpectationStateCallbackBuilder<T, List<R>> {
    val builder = ExpectationStateCallbackBuilder<T, List<R>> { storeIn ->
        RepeatedBuilder(ItemExpectationBuilder<R>().apply(itemBuilder), storeIn).build()
    }
    this += builder
    return builder
}
