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

/**
 * A type of matcher that ignores anything that matches the recognizer and
 * provides no result otherwise.
 */
class TokenRecognizerIgnored(
    /**
     * The recognizer this matcher will use
     */
    val recognizer: TokenRecognizer,
    /**
     * The behavior to follow for determining the next state
     */
    nextStateBehavior: NextStateBehavior
) : TokenMatcher(nextStateBehavior) {
    override fun match(s: String, startAt: Int): MatcherResult {
        val (_, endsAt) = recognizer.recognize(s, startAt)
            ?: return NoMatchResult
        return IgnoreMatchResult(endsAt, nextStateBehavior)
    }
}
