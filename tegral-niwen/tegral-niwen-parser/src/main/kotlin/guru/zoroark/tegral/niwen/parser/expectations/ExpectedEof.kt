package guru.zoroark.tegral.niwen.parser.expectations

import guru.zoroark.tegral.niwen.parser.ExpectationResult
import guru.zoroark.tegral.niwen.parser.ParsingContext

/**
 * An expectation that only matches if the current position is the end of the input.
 */
class ExpectedEof<T> : Expectation<T, Nothing>(null), HandlesTokenDrought {
    override fun matches(context: ParsingContext, index: Int): ExpectationResult<T> {
        if (index >= context.tokens.size) {
            return ExpectationResult.Success(
                emptyMap<NodeParameterKey<T, Nothing>, Nothing>(),
                index,
                index to index,
                "Reached end of input (EOF)"
            )
        } else {
            return ExpectationResult.DidNotMatch(
                "Did not reach end of input, was at index $index while there is/are ${context.tokens.size} token(s)",
                index
            )
        }
    }

    override val title: String = "ExpectedEof"
}
