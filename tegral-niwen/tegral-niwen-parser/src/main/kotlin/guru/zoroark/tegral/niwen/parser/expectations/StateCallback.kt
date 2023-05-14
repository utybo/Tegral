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

import guru.zoroark.tegral.niwen.parser.ExpectationResult

/**
 * A callback for manipulating and invoking side effects with state information.
 *
 * When expectations output some state in some way (e.g., an `expect(SomeNode)` that emits an instance of `SomeNode)`,
 * said state can be transformed and/or stored using a **state callback.** State callbacks take in a mutable storage and
 * a state, and returns some state. Implementations can return `state` directly or transform it and return it in some
 * way.
 *
 * @param T The context type (see [NodeParameterKey] for details)
 * @param R The initial type of the state, as input into this callback
 * @param U The returned, possibly transformed type of the state. May be the same as `R`.
 */
fun interface StateCallback<T, in R, U> {
    /**
     * Reduce the provided [state], optionally storing it in the [storage].
     *
     * **Note:** Implementations may throw any arbitrary `Exception` in `reduceState`. In case an exception occurs,
     * matching this callback is being executed for will be considered as a "Did not match".
     *
     * @param storage Map in which you can store data
     * @param state The state provided to this reducer.
     * @return The (possibly transformed) state
     */
    fun reduceState(storage: MutableMap<NodeParameterKey<T, *>, Any?>, state: R): U
}

/**
 * An implementation of [StateCallback] that stores values in the key specified in [storeValueIn].
 *
 * @property storeValueIn The key to store the value in
 * @param T The context type (see [NodeParameterKey] for details)
 * @param R The type of the state
 */
class StoreStateCallback<T, R>(private val storeValueIn: NodeParameterKey<T, R>) : StateCallback<T, R, R> {
    override fun reduceState(storage: MutableMap<NodeParameterKey<T, *>, Any?>, state: R): R {
        storage[storeValueIn] = state
        return state
    }
}

/**
 * An implementation of [StateCallback] that transforms incoming state using the provided [transformer].
 *
 * @param T The context type (see [NodeParameterKey] for details)
 * @param R The initial type of the state, as input into this callback
 * @param U The type of the output of this transformer, i.e. the type of the returned state.
 */
class TransformStateCallback<T, R, U>(private val transformer: (R) -> U) : StateCallback<T, R, U> {
    override fun reduceState(storage: MutableMap<NodeParameterKey<T, *>, Any?>, state: R): U {
        return transformer(state)
    }
}

/**
 * An implementation of [StateCallback] that composes two other state callbacks. Consider that we were only dealing with
 * regular functions, this would give:
 *
 * ```kotlin
 * fun a(state: R): U
 * fun b(state: U): V
 * ```
 *
 * Then, this class would be equivalent to the following function:
 *
 * ```kotlin
 * fun composed(state: R): U = b(a(state))
 * ```
 *
 * @param T Context type, see [NodeParameterKey] for details
 * @param R Input type of the `a` state callback
 * @param U Output type of the `a` state callback and input type of the `b` state callback
 * @param V Output type of the `b` state callback
 */
class ComposeStateCallbacks<T, R, U, V>(
    private val a: StateCallback<T, R, U>,
    private val b: StateCallback<T, U, V>
) : StateCallback<T, R, V> {
    override fun reduceState(storage: MutableMap<NodeParameterKey<T, *>, Any?>, state: R): V {
        return b.reduceState(storage, a.reduceState(storage, state))
    }
}

/**
 * Create a store map and evaluate this state callback. The map is returned.
 *
 * If `this` is `null`, an empty map is returned.
 *
 * **Note:** May throw any exception since [StateCallback.reduceState] can itself throw any exception. Consider wrapping
 * use of `createStoreMap` with `runCatching`, e.g. use [withStoreMap] instead.
 *
 * @see withStoreMap Wraps [createStoreMap] with error-handling for producing [ExpectationResult]s
 */
fun <T, R> StateCallback<T, R, *>?.createStoreMap(value: R): Map<NodeParameterKey<T, *>, Any?> {
    val map = mutableMapOf<NodeParameterKey<T, *>, Any?>()
    this?.reduceState(map, value)
    return map
}

/**
 * Create a store map (using [createStoreMap]) and call the provided [handler], returning an [ExpectationResult].
 *
 * This function properly handles any errors that may bubble up as a result of [StateCallback.reduceState] and
 * [createStoreMap] by returning an [ExpectationResult.DidNotMatch].
 */
fun <T, R> StateCallback<T, R, *>?.withStoreMap(
    value: R,
    currIndex: Int,
    handler: (Map<NodeParameterKey<T, *>, Any?>) -> ExpectationResult<T>
): ExpectationResult<T> {
    return runCatching { createStoreMap(value) }
        .map(handler)
        .getOrElse {
            ExpectationResult.DidNotMatch(
                "State callback failed: ${it.message}.\nStack trace:\n${it.stackTraceToString()}",
                currIndex
            )
        }
}
