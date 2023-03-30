package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.lexer.TokenType
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedToken
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode

/**
 * Adds an expectation to this node descriptor based on a token type.
 *
 * A token of the given token type is expected at this point.
 */
@TegralDsl
fun <T> ExpectationReceiver<T>.expect(tokenType: TokenType, withValue: String? = null): ExpectationBuilder<T, String> =
    ExpectationBuilder<T, String> {
        ExpectedToken(tokenType, withValue, storeValueIn = it)
    }.also {
        this += it
    }

/**
 * Adds an expectation to this node descriptor based on a node
 *
 * A chain of tokens that corresponds to the given node is expected at this
 * point
 */
@TegralDsl
infix fun <T, R> ExpectationReceiver<T>.expect(node: ParserNodeDeclaration<R>): ExpectationBuilder<T, R> =
    ExpectationBuilder<T, R> {
        ExpectedNode(node, it)
    }.also {
        this += it
    }