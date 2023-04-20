package guru.zoroark.tegral.niwen.parser.expectations

import guru.zoroark.tegral.niwen.parser.*
import guru.zoroark.tegral.niwen.parser.ExpectationResult.DidNotMatch
import guru.zoroark.tegral.niwen.parser.ExpectationResult.Success

/**
 * An expectation that expects another node to be present at this point.
 *
 * For example:
 *
 *  ```
 *      x = (1 + 2) + 3
 *  ```
 *
 * You could define an "assignment" node with three expectations:
 *
 * - For the first operand and the equals sign, a simple
 * [ExpectedToken]
 *
 * - For the right operand,  a `ExpectedNode(ExpressionNode)`.
 *
 * This expectation is the main way of composing nodes.
 */
class ExpectedNode<T, R>(
    private val node: ParserNodeDeclaration<R>,
    stateCallback: StateCallback<T, R, *>? = null
) : Expectation<T, R>(stateCallback) {
    override fun matches(
        context: ParsingContext,
        index: Int
    ): ExpectationResult<T> {
        val describedType = context[node]
            ?: throw NiwenParserException("Node ${node::class} is expected but not declared in the parser")
        return when (val result = describedType.expectations.applyExpectations(context, index)) {
            is Success<R> -> {
                val value = describedType.type.make(TypeDescription(result.stored))
                Success(stateCallback.createStoreMap(value), nextIndex = result.nextIndex, result.stopReason)
            }

            is DidNotMatch -> DidNotMatch(result.message, result.atTokenIndex)
        }
    }
}
