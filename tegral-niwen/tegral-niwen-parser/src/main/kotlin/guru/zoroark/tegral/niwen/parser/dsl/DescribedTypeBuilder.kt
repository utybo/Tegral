package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.lexer.TokenType
import guru.zoroark.tegral.niwen.parser.DescribedType
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.expectations.Expectation

/**
 * Builder for the description of a node. This typically should be used like
 * this:
 *
 *  ```
 *  MyNodeType {
 *      expect(TokenType)
 *      expect(MyOtherNodeType)
 *  }
 *  ```
 */
@TegralDsl
class DescribedTypeBuilder<T>(
    private val typeDeclaration: ParserNodeDeclaration<T>
) : ExpectationReceiver<T>, Buildable<DescribedType<T>> {
    /**
     * List of the expectations that should be built.
     */
    private val expectations = mutableListOf<Buildable<Expectation<T, *>>>()

    /**
     * Add an expectation that will be built when the description gets built.
     */
    override operator fun plusAssign(expectationBuilder: Buildable<Expectation<T, *>>) {
        expectations += expectationBuilder
    }

    /**
     * Builds this as a described type
     */
    override fun build(): DescribedType<T> {
        return DescribedType(
            typeDeclaration,
            expectations.map { it.build() })
    }
}

/**
 * An expectation receiver is the receiver type for all `expect` DSL constructs.
 * Use this if you want your own DSL to be able to have `expect` called on it.
 */
@TegralDsl
interface ExpectationReceiver<T> {
    /**
     * Add a buildable expectation to this receiver -- the exact meaning of this
     * depends on the implementation
     */
    operator fun plusAssign(expectationBuilder: Buildable<Expectation<T, *>>)

    /**
     * Concise notation for the [expect] function.
     */
    operator fun TokenType.unaryPlus() = expect(this)
    operator fun <R> ParserNodeDeclaration<R>.unaryPlus() = expect(this)
}

/**
 * Add an expectation directly instead of a builder. This is a shortcut for
 * `this += Buildable.of(expectation)`
 */
operator fun <T, R> ExpectationReceiver<T>.plusAssign(expectation: Expectation<T, R>) {
    this += Buildable.of(expectation)
}