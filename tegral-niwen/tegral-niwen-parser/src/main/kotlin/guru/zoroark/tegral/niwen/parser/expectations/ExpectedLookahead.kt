package guru.zoroark.tegral.niwen.parser.expectations

import guru.zoroark.tegral.niwen.parser.ExpectationResult
import guru.zoroark.tegral.niwen.parser.ParsingContext

class ExpectedLookahead<T>(
    private val lookaheadExpectations: List<Expectation<Nothing, *>>
) : Expectation<T, Nothing>(null) {
    override fun matches(context: ParsingContext, index: Int): ExpectationResult<T> {
        val result = context.applyExpectations(index, lookaheadExpectations)
        return if (result is ExpectationResult.DidNotMatch) {
            ExpectationResult.DidNotMatch(
                "Lookahead expectations failed: ${result.message} " +
                    "(while looking ahead to position ${result.atTokenIndex})",
                index
            )
        } else {
            ExpectationResult.Success(
                emptyMap<NodeParameterKey<T, Nothing>, Nothing>(),
                index,
                index to index,
                "Lookahead expectations were met"
            )
        }
    }

    override val title: String = "lookahead { ${lookaheadExpectations.size} expectation(s) }"
}
