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
 * An optional branch. This expectation is always met (always returns a
 * success), even when we have run out of tokens.
 *
 * The returned success has an empty map if we have ran out of tokens (token
 * drought) or if the branch does not match. Otherwise, the map is the one
 * returned by the branch (so it passes directly all the matched stuff).
 */
class ExpectedOptional<T, R>(private val expectations: List<Expectation<T, R>>) :
    Expectation<T, R>(), HandlesTokenDrought {
    override fun matches(
        context: ParsingContext,
        index: Int
    ): ExpectationResult<T> =
        if (index >= context.tokens.size) {
            ExpectationResult.Success(
                mapOf<NodeParameterKey<T, *>, Any?>(),
                index,
                index to index,
                "Optional branch did not match and was skipped"
            )
        } else {
            when (val result = context.applyExpectations(index, expectations)) {
                is ExpectationResult.Success -> result.copy(stopReason = "Took optional branch")
                is ExpectationResult.DidNotMatch -> ExpectationResult.Success(
                    mapOf<NodeParameterKey<T, *>, Any?>(),
                    index,
                    index to index,
                    "Optional branch did not match and was skipped"
                )
            }
        }

    override val title: String = "optional { ${expectations.size} expectation(s) }"
}
