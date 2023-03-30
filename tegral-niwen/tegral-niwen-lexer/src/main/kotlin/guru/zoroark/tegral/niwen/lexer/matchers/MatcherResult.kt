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

/**
 * Subclasses of this class represent the different possible outputs for
 * matchers.
 * @see NoMatchResult
 * @see IgnoreMatchResult
 * @see MatchedTokenResult
 */
sealed class MatcherResult

/**
 * Indicates that the match was not successful, that there was no match.
 */
object NoMatchResult : MatcherResult()

/**
 * Indicates that the match was successful, but no token should be created for
 * this match.
 *
 * @property tokenEndsAt Where the ignored token ends (exclusive, this is the
 * index where the lexer will resume)
 * @property nextStateBehavior The behavior to follow after processing this
 * match regarding states
 */
class IgnoreMatchResult(val tokenEndsAt: Int, val nextStateBehavior: NextStateBehavior) : MatcherResult()

/**
 * Indicates that the match was successful and a token was created.
 *
 * @property token The token that is the result of the match.
 */
class MatchedTokenResult(val token: Token, val nextStateBehavior: NextStateBehavior) : MatcherResult()