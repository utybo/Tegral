package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedLookahead

class LookaheadBuilder<T> : ExpectationReceiver<Nothing>, Buildable<ExpectedLookahead<T>> {
    private val expectationBuilders = mutableListOf<Buildable<Expectation<Nothing, *>>>()
    override fun build(): ExpectedLookahead<T> {
        return ExpectedLookahead(expectationBuilders.map { it.build() })
    }

    override fun plusAssign(expectationBuilder: Buildable<Expectation<Nothing, *>>) {
        expectationBuilders += expectationBuilder
    }
}

@TegralDsl
fun <T> ExpectationReceiver<T>.lookahead(builder: ExpectationReceiver<Nothing>.() -> Unit) {
    this += LookaheadBuilder<T>().apply(builder)
}
