package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedRepeated
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.RepeatedItemReceiver
import guru.zoroark.tegral.niwen.parser.expectations.StateCallback
import kotlin.reflect.typeOf


class ItemExpectationBuilder<R> : ExpectationReceiver<RepeatedItemReceiver<R>>,
    Buildable<List<Expectation<RepeatedItemReceiver<R>, *>>> {
    private val expectations = mutableListOf<Buildable<Expectation<RepeatedItemReceiver<R>, *>>>()
    override fun plusAssign(expectationBuilder: Buildable<Expectation<RepeatedItemReceiver<R>, *>>) {
        expectations += expectationBuilder
    }

    override fun build(): List<Expectation<RepeatedItemReceiver<R>, *>> {
        return expectations.map { it.build() }
    }
}

class RepeatedBuilder<T, R>(
    private val itemExpectationBuilder: ItemExpectationBuilder<R>,
    private val callback: StateCallback<T, List<R>, *>?
) : Buildable<ExpectedRepeated<T, R>> {
    override fun build(): ExpectedRepeated<T, R> {
        return ExpectedRepeated(itemExpectationBuilder.build(), callback)
    }
}

@TegralDsl
inline val <reified R> ItemExpectationBuilder<R>.item
    get() = NodeParameterKey<RepeatedItemReceiver<R>, R>(typeOf<R>(), "0")

// TODO When this function is put as an extension function, type inference fails on root nodes?
@TegralDsl
fun <T, R> ExpectationReceiver<T>.repeated(itemBuilder: ItemExpectationBuilder<R>.() -> Unit): ExpectationStateCallbackBuilder<T, List<R>> {
    val builder = ExpectationStateCallbackBuilder<T, List<R>> { storeIn ->
        RepeatedBuilder(ItemExpectationBuilder<R>().apply(itemBuilder), storeIn).build()
    }
    this += builder
    return builder
}

