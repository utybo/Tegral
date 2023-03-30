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

/**
 * A [Token] is a data class representing a token found through the
 * tokenization process of a [Lexer] and the matching process of a
 * [TokenMatcher]. It has
 * information on where the token begins, where it ends, its type, and what it
 * actually represents.
 */
data class Token(
    /**
     * The string this token represents. This is exactly what was match and, as
     * such, is a substring of the original lexed string
     */
    val string: String,
    /**
     * The index where [string] starts in the original string.
     *
     * For example: Token "world" in "Hello world" has startsAt equal to 6
     */
    val startsAt: Int,
    /**
     * The index after [string] ends in the original string (i.e. exclusive).
     *
     * For example: Token "Hello" in "Hello world" has endsAt equal to 5
     */
    val endsAt: Int,
    /**
     * The token type this token corresponds to
     */
    val tokenType: TokenType
)
