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
import guru.zoroark.tegral.niwen.parser.ExpectationResult.DidNotMatch
import guru.zoroark.tegral.niwen.parser.ExpectationResult.Success
import guru.zoroark.tegral.niwen.parser.NiwenParserException
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.ParsingContext
import guru.zoroark.tegral.niwen.parser.TypeDescription

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
 * - For the right operand, an `ExpectedNode(ExpressionNode)`.
 *
 * This expectation is the main way of composing nodes.
 */
class ExpectedNode<T, R>(
    private val node: ParserNodeDeclaration<R>,
    stateCallback: StateCallback<T, R, *>? = null
) : Expectation<T, R>(stateCallback) {
    override val title: String = "ExpectedNode(${node.name})"

    override fun matches(
        context: ParsingContext,
        index: Int
    ): ExpectationResult<T> {
        val describedType = context[node]
            ?: throw NiwenParserException("Node ${node::class} is expected but not declared in the parser")
        return when (val result = context.applyExpectations(index, describedType.expectations)) {
            is Success<R> -> {
                val value = describedType.type.make(TypeDescription(result.stored))
                stateCallback.withStoreMap(value, index) { storeMap ->
                    Success(
                        storeMap,
                        nextIndex = result.nextIndex,
                        index to result.nextIndex,
                        "Node matched successfully"
                    )
                }
            }

            is DidNotMatch -> DidNotMatch(result.message, result.atTokenIndex)
        }
    }
}
