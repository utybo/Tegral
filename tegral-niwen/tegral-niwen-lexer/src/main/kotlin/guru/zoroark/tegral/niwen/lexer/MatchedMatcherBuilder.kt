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

package guru.zoroark.tegral.niwen.lexer

import guru.zoroark.tegral.niwen.lexer.matchers.TokenMatcher
import guru.zoroark.tegral.niwen.lexer.matchers.TokenRecognizer
import guru.zoroark.tegral.niwen.lexer.matchers.TokenRecognizerMatched

/**
 * A simple builder for matchers, whose main purpose is to provide a way to
 * select a "next state" for a matcher through the [thenState] function.
 */
class MatchedMatcherBuilder(
    /**
     * The original recognizer that should be used by the matcher that will be
     * built
     */
    baseRecognizer: TokenRecognizer,
    /**
     * The token type this matcher will be matched against.
     */
    private val matchesToTokenType: TokenType
) : TokenMatcherBuilder(baseRecognizer) {
    override fun build(): TokenMatcher =
        TokenRecognizerMatched(baseRecognizer, matchesToTokenType, nextStateBehavior)
}
