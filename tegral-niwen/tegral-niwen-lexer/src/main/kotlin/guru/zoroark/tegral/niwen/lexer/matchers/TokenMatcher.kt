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
 * A token matcher is an object that can determine whether a string at a given
 * offset matches some pattern. The matcher then returns a corresponding token
 * or null if no match is found.
 */
abstract class TokenMatcher(
    /**
     * The behavior for choosing a next state that will be followed upon a
     * successful match.
     *
     * @see NextStateBehavior
     */
    protected val nextStateBehavior: NextStateBehavior = NoStateChange
) {
    /**
     * This function determines whether the string [s], starting at the index
     * [startAt] (inclusive), matches some pattern. The exact pattern is
     * dependent on the implementation.
     *
     * @param s The string to analyze
     * @param startAt The index after which the string must be considered
     * @return Null if no match is possible, or a [Token][guru.zoroark.tegral.niwen.lexer.Token] that corresponds
     * to the matched substring.
     */
    abstract fun match(s: String, startAt: Int): MatcherResult
}
