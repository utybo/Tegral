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
import kotlin.test.*

class RepeatedRecognizerTest {

    @Test
    fun repeated_string_recognizer_test() {
        val thi = tokenType()
        val thello = tokenType()
        val lexer = niwenLexer {
            state {
                "hi ".repeated isToken thi
                "hello" isToken thello
                " ".ignore
            }
        }
        val string = "hi hi hi hi hi hi hello hi hi hello hello"
        val expected = listOf(
            Token("hi hi hi hi hi hi ", 0, 18, thi),
            Token("hello", 18, 23, thello),
            Token("hi hi ", 24, 30, thi),
            Token("hello", 30, 35, thello),
            Token("hello", 36, 41, thello)
        )
        val actual = lexer.tokenize(string)
        assertEquals(expected, actual)
    }

    @Test
    fun repeated_string_recognizer_with_additional_parameters_test() {
        val thi = tokenType()
        val thello = tokenType()
        val lexer = niwenLexer {
            state {
                "hi ".repeated(min = 3, max = 5) isToken thi
                "hello" isToken thello
                " ".ignore
            }
        }
        val stringFailNotEnough = "hello hi hi hello"
        assertFailsWith<NiwenLexerNoMatchException> {
            lexer.tokenize(stringFailNotEnough)
        }
        val stringFailTooMany = "hi hi hi hi hi hi hello"
        assertFailsWith<NiwenLexerNoMatchException> {
            lexer.tokenize(stringFailTooMany)
        }
        val stringSuccess =
            "hellohi hi hi hello hi hi hi hi hello hello hi hi hi hi hi hello hi hi hi hi hi "
        val expected = listOf(
            Token("hello", 0, 5, thello),
            Token("hi hi hi ", 5, 14, thi),
            Token("hello", 14, 19, thello),
            Token("hi hi hi hi ", 20, 32, thi),
            Token("hello", 32, 37, thello),
            Token("hello", 38, 43, thello),
            Token("hi hi hi hi hi ", 44, 59, thi),
            Token("hello", 59, 64, thello),
            Token("hi hi hi hi hi ", 65, 80, thi)
        )
        val result = lexer.tokenize(stringSuccess)
        assertEquals(expected, result)
    }

    @Test
    fun any_repeated_recognizer_test() {
        val tgreet = tokenType()
        val ttest = tokenType()
        val lexer = niwenLexer {
            state {
                anyOf("hello", "hi", "hey").repeated isToken tgreet
                "test" isToken ttest
                " ".ignore
            }
        }
        val string = "hellohello hihihi hey hihi test heyheyhey"
        val expected = listOf(
            Token("hellohello", 0, 10, tgreet),
            Token("hihihi", 11, 17, tgreet),
            Token("hey", 18, 21, tgreet),
            Token("hihi", 22, 26, tgreet),
            Token("test", 27, 31, ttest),
            Token("heyheyhey", 32, 41, tgreet)
        )
        val result = lexer.tokenize(string)
        assertEquals(expected, result)
    }

    @Test
    fun any_repeated_recognizer_with_additional_parameters_test() {
        val tgreet = tokenType()
        val ttest = tokenType()
        val lexer = niwenLexer {
            state {
                anyOf("hello", "hi", "hey").repeated(min = 2, max = 4) isToken tgreet
                "test" isToken ttest
                " ".ignore
            }
        }
        val stringNotEnough = "hellohello hihihi hey hihihihi"
        assertFailsWith<NiwenLexerNoMatchException> {
            lexer.tokenize(stringNotEnough)
        }
        val stringTooMany = "hellohellohello heyhey hihihihihi hihihi"
        assertFailsWith<NiwenLexerNoMatchException> {
            lexer.tokenize(stringTooMany)
        }
        val stringSuccess = "hellohello hihihi hihi test heyheyheyhey"
        val expected = listOf(
            Token("hellohello", 0, 10, tgreet),
            Token("hihihi", 11, 17, tgreet),
            Token("hihi", 18, 22, tgreet),
            Token("test", 23, 27, ttest),
            Token("heyheyheyhey", 28, 40, tgreet)
        )
        val result = lexer.tokenize(stringSuccess)
        assertEquals(expected, result)
    }
}