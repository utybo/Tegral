package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedEof

/**
 * Expect the end-of-file to be reached at this point.
 */
@TegralDsl
fun <T> ExpectationReceiver<T>.expectEof() {
    this += Buildable { ExpectedEof() }
}
