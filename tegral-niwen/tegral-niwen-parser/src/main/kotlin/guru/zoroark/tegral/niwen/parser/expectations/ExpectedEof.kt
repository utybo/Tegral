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
