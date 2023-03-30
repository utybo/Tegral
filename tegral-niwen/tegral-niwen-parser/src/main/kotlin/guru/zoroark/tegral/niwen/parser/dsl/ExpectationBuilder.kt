package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.RawKey
import guru.zoroark.tegral.niwen.parser.expectations.asKey
import kotlin.reflect.KProperty1
import kotlin.reflect.full.withNullability

/**
 * Simple builder for an expectation. The only real use of this class is to add
 * the "storeIn" thing on-the-fly-ish, after the call to "expect".
 */
class ExpectationBuilder<T, R>(private val builderFunc: (NodeParameterKey<T, R>?) -> Expectation<T, R>) : Buildable<Expectation<T, R>> {
    private var storeIn: NodeParameterKey<T, R>? = null

    /**
     * Signals that the expectation should store its result using the given
     * argument name.
     */
    infix fun storeIn(argName: NodeParameterKey<T, R>) {
        storeIn = argName
    }

    infix fun storeIn(rawArg: RawKey<R>) {
        storeIn = NodeParameterKey(rawArg.outputType, rawArg.name)
    }

    infix fun storeIn(property: KProperty1<T, R?>) {
        // We are only writing to this property later on, so erasing nullability here is ok
        val base = property.asKey()
        storeIn = NodeParameterKey(base.outputType, base.name)
    }


    /**
     * Builds this expectation
     */
    override fun build(): Expectation<T, R> {
        return builderFunc(storeIn)
    }
}
