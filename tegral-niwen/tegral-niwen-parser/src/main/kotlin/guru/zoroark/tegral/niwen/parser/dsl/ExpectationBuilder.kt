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

interface StateCallbackBuilder<T, R> {
    fun <U> compose(stateCallback: StateCallback<T, R, U>): StateCallbackBuilder<T, U>

    /**
     * Signals that the expectation should store its result using the given
     * argument name.
     */
    @TegralDsl
    infix fun storeIn(argName: NodeParameterKey<T, R>) =
        compose(StoreStateCallback(argName))

    @TegralDsl
    infix fun storeIn(rawArg: RawKey<R>) {
        compose(StoreStateCallback(NodeParameterKey(rawArg.outputType, rawArg.name)))
    }

    @TegralDsl
    infix fun storeIn(property: KProperty1<T, R?>) =
        // We are only writing to this property later on, so erasing nullability here is ok
        property.asKey().let { key ->
            compose(StoreStateCallback(NodeParameterKey(key.outputType, key.name)))
        }

    @TegralDsl
    infix fun <V> transform(transformer: (R) -> V) =
        compose(TransformStateCallback(transformer))
}

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
