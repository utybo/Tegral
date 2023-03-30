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
import guru.zoroark.tegral.niwen.lexer.matchers.matches
import kotlin.test.Test
import kotlin.test.assertEquals

class IgnoreMatcherTest {
    @Test
    fun ignore_matcher_on_string_in_single_state() {
        val tone = tokenType()
        val ttwo = tokenType()
        val lexer = niwenLexer {
            state {
                "a" isToken tone
                "b" isToken ttwo
                " ".ignore
            }
        }
        val string = "ab ba aa bb"
        val expected = listOf(
            Token("a", 0, 1, tone),
            Token("b", 1, 2, ttwo),
            Token("b", 3, 4, ttwo),
            Token("a", 4, 5, tone),
            Token("a", 6, 7, tone),
            Token("a", 7, 8, tone),
            Token("b", 9, 10, ttwo),
            Token("b", 10, 11, ttwo)
        )
        val result = lexer.tokenize(string)
        assertEquals(expected, result)
    }

    @Test
    fun ignore_matcher_on_string_in_multiple_states() {
        val tkey = tokenType()
        val tvalue = tokenType()
        val svalue = stateLabel()
        val lexer = niwenLexer {
            default state {
                matches("[a-zA-Z_ ]+") isToken tkey
                "->".ignore thenState svalue
            }
            svalue state {
                matches("[a-zA-Z_ ]+") isToken tvalue
                "\n".ignore thenState default
            }
        }
        val string = """
            key->value
            one->two
            Example_Yes->B e e_
        """.trimIndent()
        val expected = listOf(
            Token("key", 0, 3, tkey),
            Token("value", 5, 10, tvalue),
            Token("one", 11, 14, tkey),
            Token("two", 16, 19, tvalue),
            Token("Example_Yes", 20, 31, tkey),
            Token("B e e_", 33, 39, tvalue)
        )
        val result = lexer.tokenize(string)
        assertEquals(expected, result)
    }

    @Test
    fun ignore_matcher_on_any_matcher_in_single_state() {
        val tspace = tokenType()
        val tword = tokenType()
        val lexer = niwenLexer {
            default state {
                anyOf("banana", "apple", "strawberry", "raspberry").ignore
                " " isToken tspace
                matches("[a-zA-Z_]+") isToken tword
            }
        }
        val string = "banana test apple yes raspberry"
        val expected = listOf(
            Token(" ", 6, 7, tspace),
            Token("test", 7, 11, tword),
            Token(" ", 11, 12, tspace),
            Token(" ", 17, 18, tspace),
            Token("yes", 18, 21, tword),
            Token(" ", 21, 22, tspace)
        )
        val result = lexer.tokenize(string)
        assertEquals(expected, result)
    }
}

