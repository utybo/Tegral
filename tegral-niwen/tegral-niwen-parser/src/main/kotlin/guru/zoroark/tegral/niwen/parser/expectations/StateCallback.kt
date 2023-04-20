package guru.zoroark.tegral.niwen.parser.expectations

interface StateCallback<T, in R, U> {
    fun reduceState(storage: MutableMap<NodeParameterKey<T, *>, Any?>, state: R): U
}

class StoreStateCallback<T, R>(val storeValueIn: NodeParameterKey<T, R>) : StateCallback<T, R, R> {
    override fun reduceState(storage: MutableMap<NodeParameterKey<T, *>, Any?>, state: R): R {
        storage[storeValueIn] = state
        return state
    }
}

class TransformStateCallback<T, R, U>(private val transformer: (R) -> U) : StateCallback<T, R, U> {
    override fun reduceState(storage: MutableMap<NodeParameterKey<T, *>, Any?>, state: R): U {
        return transformer(state)
    }
}

class ComposeStateCallbacks<T, R, U, V>(
    private val a: StateCallback<T, R, U>,
    private val b: StateCallback<T, U, V>
) : StateCallback<T, R, V> {
    override fun reduceState(storage: MutableMap<NodeParameterKey<T, *>, Any?>, state: R): V {
        return b.reduceState(storage, a.reduceState(storage, state))
    }
}

fun <T, R> StateCallback<T, R, *>?.createStoreMap(value: R): Map<NodeParameterKey<T, *>, Any?> {
    val map = mutableMapOf<NodeParameterKey<T, *>, Any?>()
    this?.reduceState(map, value)
    return map
}