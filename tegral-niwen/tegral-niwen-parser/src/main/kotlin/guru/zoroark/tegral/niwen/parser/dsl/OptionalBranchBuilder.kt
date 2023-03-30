package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedOptional

/**
 * Create an optional branch. If the optional branch matches, what it stores
 * will is passed transparently, just like if the `optional { }` was not there.
 * If the optional branch does not match, nothing happens, just like if the
 * entire optional branch was not there.
 *
 * Typical usage may look like:
 *
 *  ```
 *  MyNode {
 *      // Everything here must be present in order to have a match...
 *      expect(...)
 *      expect(...)
 *      optional {
 *          // This branch is optional
 *          expect(...)
 *      }
 *  }
 */
@TegralDsl
fun <T> ExpectationReceiver<T>.optional(optionalBlock: OptionalBranchBuilder<T>.() -> Unit) {
    this += OptionalBranchBuilder<T>().apply(optionalBlock)
}

/**
 * Builder class for an optional expectation.
 */
class OptionalBranchBuilder<T> :
    ExpectationReceiver<T>, Buildable<Expectation<T, *>> {
    private val expectations = mutableListOf<Buildable<Expectation<T, *>>>()

    override fun plusAssign(expectationBuilder: Buildable<Expectation<T, *>>) {
        expectations += expectationBuilder
    }

    /**
     * Build this expected optional
     */
    override fun build(): Expectation<T, *> {
        return ExpectedOptional(expectations.map { it.build() })
    }

}