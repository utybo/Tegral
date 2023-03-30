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

package guru.zoroark.tegral.niwen.lexer.matchers

import guru.zoroark.tegral.niwen.lexer.Token
import guru.zoroark.tegral.niwen.lexer.TokenType

/**
 * This class can be used to associate a [TokenRecognizer] with a token type,
 * forming a complete [TokenMatcher]
 */
class TokenRecognizerMatched(
    /**
     * The recognizer this matcher will use
     */
    val recognizer: TokenRecognizer,
    /**
     * The token type to match recognized tokens to
     */
    val tokenType: TokenType,
    /**
     * The behavior to follow for determining the next state
     */
    nextStateBehavior: NextStateBehavior = NoStateChange
) : TokenMatcher(nextStateBehavior) {
    override fun match(s: String, startAt: Int): MatcherResult =
        recognizer.recognize(s, startAt)?.let { (recognizedSubstring, endsAt) ->
            MatchedTokenResult(
                Token(recognizedSubstring, startAt, endsAt, tokenType),
                nextStateBehavior
            )
        } ?: NoMatchResult

}
