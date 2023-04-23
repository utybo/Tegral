package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.Token
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.HandlesTokenDrought
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey

/**
 * This object contains the information that is passed to expectations, and is
 * global over a single parser run.
 *
 * @property tokens The list of tokens that should be parsed
 *
 * @property typeMap A map with all of the known declared types and their
 * description
 */
class ParsingContext(
    val tokens: List<Token>,
    private val typeMap: Map<ParserNodeDeclaration<*>, DescribedType<*>>,
    val branchSeeker: BranchSeeker? = null
) {
    operator fun <T> get(declaration: ParserNodeDeclaration<T>): DescribedType<T>? {
        return typeMap[declaration] as? DescribedType<T>
    }

    fun <T> enterBranch(branchTitle: String, branch: () -> ExpectationResult<T>): ExpectationResult<T> {
        branchSeeker?.stepIn(branchTitle)
        val result = branch()
        when (result) {
            is ExpectationResult.DidNotMatch -> branchSeeker?.stepOut(
                BranchSeeker.Status.DID_NOT_MATCH,
                result.message,
                emptyMap()
            )

            is ExpectationResult.Success -> branchSeeker?.stepOut(
                BranchSeeker.Status.SUCCESS,
                result.stopReason,
                result.stored.toMap(),
            )
        }
        return result
    }

    private fun <T> applyExpectation(atIndex: Int, expectation: Expectation<T, *>): ExpectationResult<T> {
        return enterBranch(expectation.title) {
            if (atIndex >= tokens.size && expectation !is HandlesTokenDrought) {
                val message = "Expected more tokens, but ran out of tokens"
                return@enterBranch ExpectationResult.DidNotMatch(message, atIndex)
            }

            expectation.matches(this, atIndex)
        }
    }


    /**
     * Apply each expectation of this list to the given context starting at the
     * given index.
     */
    fun <T> applyExpectations(
        startAt: Int,
        expectations: List<Expectation<T, *>>
    ): ExpectationResult<T> {
        var index = startAt
        val map = mutableMapOf<NodeParameterKey<T, *>, Any?>()
        for (expectation in expectations) {
            val result = applyExpectation(index, expectation)

            if (result is ExpectationResult.Success) {
                map.putAll(result.stored)
                index = result.nextIndex
            } else {
                return result
            }
        }
        return ExpectationResult.Success(map, index, startAt to index, "All expectations in branch matched")
    }
}
