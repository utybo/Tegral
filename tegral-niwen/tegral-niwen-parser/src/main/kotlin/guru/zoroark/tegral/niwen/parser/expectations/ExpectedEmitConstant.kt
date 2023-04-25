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
 * An expectation that always matches and always emits the provided [value].
 */
class ExpectedEmitConstant<T, R>(
    /**
     * The value that will always be emitted.
     */
    val value: R,
    stateCallback: StateCallback<T, R, *>? = null
) : Expectation<T, R>(stateCallback) {
    override fun matches(context: ParsingContext, index: Int): ExpectationResult<T> {
        return ExpectationResult.Success(
            stateCallback.createStoreMap(value),
            index,
            index to index,
            "Emitted value $value"
        )
    }

    override val title: String = "emit($value)"
}
