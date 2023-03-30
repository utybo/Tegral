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

import guru.zoroark.tegral.niwen.lexer.matchers.anyOf
import guru.zoroark.tegral.niwen.lexer.matchers.repeated
import kotlin.test.Test
import kotlin.test.assertEquals

class RangeRecognizerTests {
    @Test
    fun range_can_be_used_as_bare_recognizer() {
        val ta = tokenType()
        val tdigit = tokenType()
        val lexer = niwenLexer {
            state {
                "a" isToken ta
                '0'..'9' isToken tdigit
            }
        }
        val string = "a1aa4a12345a67a890"
        val expected = listOf(
            Token("a", 0, 1, ta),
            Token("1", 1, 2, tdigit),
            Token("a", 2, 3, ta),
            Token("a", 3, 4, ta),
            Token("4", 4, 5, tdigit),
            Token("a", 5, 6, ta),
            Token("1", 6, 7, tdigit),
            Token("2", 7, 8, tdigit),
            Token("3", 8, 9, tdigit),
            Token("4", 9, 10, tdigit),
            Token("5", 10, 11, tdigit),
            Token("a", 11, 12, ta),
            Token("6", 12, 13, tdigit),
            Token("7", 13, 14, tdigit),
            Token("a", 14, 15, ta),
            Token("8", 15, 16, tdigit),
            Token("9", 16, 17, tdigit),
            Token("0", 17, 18, tdigit)
        )
        val actual = lexer.tokenize(string)
        assertEquals(expected, actual)
    }

    @Test
    fun range_can_be_used_with_repetition() {
        val top = tokenType()
        val tsign = tokenType()
        val tnumber = tokenType()
        val expectNumber = stateLabel()
        val expectOperation = stateLabel()
        val lexer = niwenLexer {
            default state expectNumber
            expectNumber state {
                ('0'..'9').repeated isToken tnumber thenState expectOperation
                anyOf("+", "-") isToken tsign
                " ".repeated.ignore
            }
            expectOperation state {
                anyOf("+", "-", "*", "/") isToken top thenState expectNumber
                " ".repeated.ignore
            }
        }
        val string = "-136 + +287 * -35 / 42 + 9393913"
        val expected = listOf(
            Token("-", 0, 1, tsign),
            Token("136", 1, 4, tnumber),
            Token("+", 5, 6, top),
            Token("+", 7, 8, tsign),
            Token("287", 8, 11, tnumber),
            Token("*", 12, 13, top),
            Token("-", 14, 15, tsign),
            Token("35", 15, 17, tnumber),
            Token("/", 18, 19, top),
            Token("42", 20, 22, tnumber),
            Token("+", 23, 24, top),
            Token("9393913", 25, 32, tnumber)
        )
        val actual = lexer.tokenize(string)
        assertEquals(expected, actual)
    }
}