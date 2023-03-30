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
    storeValueIn: NodeParameterKey<T, R>? = null
) : Expectation<T, R>(storeValueIn) {
    override fun matches(
        context: ParsingContext,
        index: Int
    ): ExpectationResult<T> {
        val describedType = context[node]
            ?: throw NiwenParserException("Node ${node::class} is expected but not declared in the parser")
        return when (val result = describedType.expectations.applyExpectations(context, index)) {
            is Success<R> -> {
                val value = describedType.type.make(TypeDescription(result.stored))
                val res = storeValueIn?.let {
                    mapOf<NodeParameterKey<T, *>, Any?>(storeValueIn to value)
                } ?: mapOf()
                Success(res, nextIndex = result.nextIndex)
            }

            is DidNotMatch -> DidNotMatch(result.message, result.atTokenIndex)
        }
    }
}
