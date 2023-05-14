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
import guru.zoroark.tegral.niwen.parser.expectations.ComposeStateCallbacks
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.RawKey
import guru.zoroark.tegral.niwen.parser.expectations.StateCallback
import guru.zoroark.tegral.niwen.parser.expectations.StoreStateCallback
import guru.zoroark.tegral.niwen.parser.expectations.TransformStateCallback
import guru.zoroark.tegral.niwen.parser.expectations.asKey
import kotlin.reflect.KProperty1

/**
 * A builder for composing [StateCallback]s.
 *
 * Unlocks the DSLs for creating and composing state callbacks.
 */
interface StateCallbackBuilder<T, R> {
    /**
     * Compose the current state callback with the provided [stateCallback].
     */
    fun <U> compose(stateCallback: StateCallback<T, R, U>): StateCallbackBuilder<T, U>

    /**
     * Signals that the expectation should store its result using the given argument key.
     */
    @TegralDsl
    infix fun storeIn(argName: NodeParameterKey<T, R>) =
        compose(StoreStateCallback(argName))

    /**
     * Signals that the expectation should store its result using the given raw key.
     */
    @TegralDsl
    infix fun storeIn(rawArg: RawKey) {
        compose(StoreStateCallback(NodeParameterKey(rawArg.outputType, rawArg.name)))
    }

    /**
     * Signals that the expectation should store its result using the given property.
     */
    @TegralDsl
    infix fun storeIn(property: KProperty1<T, R?>) =
        // We are only writing to this property later on, so erasing nullability here is ok
        property.asKey().let { key ->
            compose(StoreStateCallback(NodeParameterKey(key.outputType, key.name)))
        }

    /**
     * Transform the current value using the given lambda.
     *
     * You can then chain other calls (e.g. [storeIn]) off of this `transform` call.
     */
    @TegralDsl
    infix fun <V> transform(transformer: (R) -> V) =
        compose(TransformStateCallback(transformer))
}

/**
 * Implementation of [StateCallbackBuilder] that recursively composes itself to build a [StateCallback]
 */
class ComposableStateCallbackBuilder<T, R, U>(
    private val current: StateCallback<T, R, U>
) : StateCallbackBuilder<T, U>, Buildable<StateCallback<T, R, *>> {
    private var upper: ComposableStateCallbackBuilder<T, R, *>? = null
    override fun <V> compose(stateCallback: StateCallback<T, U, V>): ComposableStateCallbackBuilder<T, R, V> {
        return ComposableStateCallbackBuilder(ComposeStateCallbacks(current, stateCallback))
            .also { upper = it }
    }

    override fun build(): StateCallback<T, R, *> =
        upper?.build() ?: current
}

/**
 * A builder for state callbacks that get added to an expectation.
 *
 * This is the object used for the right-hand side of the `expect() ...` DSL.
 */
class ExpectationStateCallbackBuilder<T, R>(
    private val builderFunc: (StateCallback<T, R, *>?) -> Expectation<T, R>
) : Buildable<Expectation<T, R>>, StateCallbackBuilder<T, R> {
    private var callbackBuilder: ComposableStateCallbackBuilder<T, R, *>? = null

    /**
     * Builds this expectation
     */
    override fun build(): Expectation<T, R> {
        return builderFunc(callbackBuilder?.build())
    }

    override fun <V> compose(stateCallback: StateCallback<T, R, V>): StateCallbackBuilder<T, V> {
        return ComposableStateCallbackBuilder(stateCallback)
            .also { callbackBuilder = it }
    }
}
