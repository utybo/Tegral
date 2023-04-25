/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    /**
     * The [BranchSeeker] instance associated with this parsing context.
     *
     * If null, debugging is disabled.
     *
     * You should rarely need to manually update the rare seeker. [enterBranch] should be enough for most purposes
     */
    val branchSeeker: BranchSeeker? = null
) {
    /**
     * Retrieve the provided node declaration, or null if no such declaration could be found.
     */
    operator fun <T> get(declaration: ParserNodeDeclaration<T>): DescribedType<T>? {
        return typeMap[declaration] as? DescribedType<T>
    }

    /**
     * If debugging is enabled, execute a set of expectations "within" a special node. Otherwise, directly executed the
     * provided branch.
     *
     * This can be used to provide additional context for several "execution runs" that may occur within an expectation.
     * For example, this is used in `either { ... }` 's expectation to identify each branch.
     */
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
     * Apply each expectation of this list to the given context, starting at the given index.
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
        return ExpectationResult.Success(
            map,
            index,
            startAt to index,
            "All expectations in branch matched"
        )
    }
}
