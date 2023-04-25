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

import guru.zoroark.tegral.niwen.lexer.TokenType
import guru.zoroark.tegral.niwen.parser.ExpectationResult
import guru.zoroark.tegral.niwen.parser.ParsingContext

/**
 * An expectation that expects a token to be present at this point.
 *
 * For example:
 *
 *  ```
 *  "Hello!"
 *  Tokens: [QUOTE = ", STRING_CONTENT = Hello!, QUOTE = "]
 *  ```
 *
 * You could define a "string value" node with three expectations:
 *
 * - For the first `"`, an expectation for a QUOTE token (see
 * [ExpectedToken])
 *
 * - For the string content in the middle, an expectation for a STRING_CONTENT
 * token.
 *
 * - For the second `"`, an expectation for a QUOTE token.
 */
class ExpectedToken<T>(
    private val tokenType: TokenType,
    private val withValue: String? = null,
    stateCallback: StateCallback<T, String, *>? = null
) : Expectation<T, String>(stateCallback) {
    override fun matches(
        context: ParsingContext,
        index: Int
    ): ExpectationResult<T> = with(context) {
        val token = tokens[index]
        if (token.tokenType == tokenType && (withValue == null || withValue == token.string)) {
            return ExpectationResult.Success(
                stateCallback.createStoreMap(token.string),
                index + 1,
                index to index + 1,
                if (withValue == null) {
                    "Token '${token.string}' is of correct type ${token.tokenType}"
                } else {
                    "Token '${token.string} is of correct type ${token.tokenType} and has correct 'withValue'"
                }
            )
        }
        return ExpectationResult.DidNotMatch(
            if (withValue == null) {
                "At index $index, expected token of type $tokenType, but encountered ${token.tokenType} " +
                    "('${token.string}') instead"
            } else {
                "At index $index, expected token of type $tokenType with value '$withValue', but encountered " +
                    "${token.tokenType} ('${token.string}')"
            },
            index
        )
    }

    override val title: String =
        if (withValue == null) {
            "expect($tokenType)"
        } else {
            "expect($tokenType, withValue = '$withValue')"
        }
}
