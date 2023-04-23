package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.lexer.TokenType
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedEmitConstant
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedToken

/**
 * Adds an expectation to this node descriptor based on a token type.
 *
 * A token of the given token type is expected at this point.
 */
@TegralDsl
fun <T> ExpectationReceiver<T>.expect(tokenType: TokenType, withValue: String? = null) =
    ExpectationStateCallbackBuilder<T, String> {
        ExpectedToken(tokenType, withValue, it)
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
infix fun <T, R> ExpectationReceiver<T>.expect(node: ParserNodeDeclaration<R>) =
    ExpectationStateCallbackBuilder<T, R> {
        ExpectedNode(node, it)
    }.also {
        this += it
    }

@TegralDsl
fun <T, R> ExpectationReceiver<T>.emit(value: R) =
    ExpectationStateCallbackBuilder<T, R> {
        ExpectedEmitConstant(value, it)
    }.also {
        this += it
    }
