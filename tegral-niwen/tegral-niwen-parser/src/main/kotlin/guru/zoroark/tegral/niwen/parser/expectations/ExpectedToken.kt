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
        if (token.tokenType == tokenType && (withValue == null || withValue == token.string))
            return ExpectationResult.Success(
                stateCallback.createStoreMap(token.string),
                index + 1
            )
        return ExpectationResult.DidNotMatch(
            if (withValue == null) "At index $index, expected token of type $tokenType, but encountered ${token.tokenType} instead"
            else "At index $index, expected token of type $tokenType with value $withValue, but encountered ${token.tokenType} with value '${token.string}'",
            index
        )
    }
}