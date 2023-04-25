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
 * An expectation that takes the result of some [branches][EitherBranch].
 *
 * The expectation returns the result of the first successful branch, or a
 * DidNotMatch with a message that contains all the DidNotMatch messages of
 * the branches.
 *
 * Branches are checked in the list's order.
 *
 * @property branches The branches of this expectation. They are always
 * checked in the list's order.
 */
class ExpectedEither<T>(private val branches: List<EitherBranch<T>>) : Expectation<T, Nothing>() {
    override fun matches(
        context: ParsingContext,
        index: Int
    ): ExpectationResult<T> {
        val failures = mutableListOf<ExpectationResult.DidNotMatch<T>>()
        branches.withIndex().forEach { (i, branch) ->

            when (
                val result = context.enterBranch("Branch $i") {
                    context.applyExpectations(index, branch.expectations)
                }
            ) {
                is ExpectationResult.Success -> {
                    return result.copy(stopReason = "Branch $i matched")
                }

                is ExpectationResult.DidNotMatch -> {
                    failures += result
                }
            }
        }
        return ExpectationResult.DidNotMatch(
            "None of the ${branches.size} branches matched.",
            index
        )
    }

    override val title: String = "either { ${branches.size} branch(es) }"
}

/**
 * A branch for the [ExpectedEither] expectation.
 *
 * @property expectations The expectations that are a part of this branch.
 */
class EitherBranch<T>(val expectations: List<Expectation<T, *>>)
