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

package guru.zoroark.tegral.niwen.parser.expectations

import guru.zoroark.tegral.niwen.parser.ExpectationResult
import guru.zoroark.tegral.niwen.parser.ParsingContext

/**
 * A "look-ahead" branch. Similar to lookaheads in regular expressions, [ExpectedLookahead] will verify expectations,
 * but not advanced the "cursor" to the next token. You can think of it as check against "what will come next", while
 * staying where we are.
 */
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
