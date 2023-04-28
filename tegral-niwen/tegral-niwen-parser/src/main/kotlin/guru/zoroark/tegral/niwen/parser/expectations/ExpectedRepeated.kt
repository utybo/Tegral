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
 * Receiver interface for the content of a `repeated { }` block.
 *
 * This interface is present to properly isolate the content of a `repeated` block from its parent and make sure its
 * side effects only impact the `item` of the current branch.
 */
sealed interface RepeatedItemReceiver<R>

/**
 * A repeated branch that runs 0+ iterations of some expectations and collects the results into a list. Said list can
 * then be stored in the model results.
 */
class ExpectedRepeated<T, R>(
    private val minIterations: Int?,
    private val maxIterations: Int?,
    private val repeatableExpectations: List<Expectation<RepeatedItemReceiver<R>, *>>,
    stateCallback: StateCallback<T, List<R>, *>?
) : Expectation<T, List<R>>(stateCallback), HandlesTokenDrought {
    override fun matches(context: ParsingContext, index: Int): ExpectationResult<T> {
        var currIndex = index
        val valueAcc = mutableListOf<R>()
        var matchCount = 0
        while (currIndex < context.tokens.size) {
            when (
                val next = context.enterBranch("Iteration $matchCount (at index $currIndex)") {
                    context.applyExpectations(currIndex, repeatableExpectations)
                }
            ) {
                is ExpectationResult.DidNotMatch -> break

                is ExpectationResult.Success -> {
                    currIndex = next.nextIndex
                    matchCount++
                    next.stored.entries.firstOrNull()?.let { valueAcc += it.value as R }
                }
            }
        }
        return when {
            minIterations != null && matchCount < minIterations -> ExpectationResult.DidNotMatch(
                "Expected at least $minIterations iterations, but only got $matchCount",
                currIndex
            )

            maxIterations != null && matchCount > maxIterations -> ExpectationResult.DidNotMatch(
                "Expected at most $maxIterations iterations, but got $matchCount repetitions",
                currIndex
            )

            else -> ExpectationResult.Success(
                stateCallback.createStoreMap(valueAcc),
                currIndex,
                index to currIndex,
                "Repeated $matchCount time(s)."
            )
        }
    }

    override val title: String = "repeated { ${repeatableExpectations.size} expectation(s) }"
}
