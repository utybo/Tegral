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

import guru.zoroark.tegral.niwen.lexer.matchers.StringRecognizer
import guru.zoroark.tegral.niwen.lexer.matchers.TokenRecognizerMatched
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LexerStateTest {
    @Test
    fun supports_constructing_one_default_state() {
        val dottoken = tokenType()
        val lexer = niwenLexer {
            default state {
                "." isToken dottoken
            }
        }

        val tokens = lexer.tokenize("...")
        assertEquals(
            listOf(
                Token(".", 0, 1, dottoken),
                Token(".", 1, 2, dottoken),
                Token(".", 2, 3, dottoken)
            ),
            tokens
        )
    }

    @Test
    fun fails_to_construct_unlabeled_then_labeled_default() {
        val ttype = tokenType()
        assertFailsWith<NiwenLexerException> {
            niwenLexer {
                state {
                    "." isToken ttype
                }
                default state {
                    "." isToken ttype
                }
            }
        }
    }

    @Test
    fun fails_to_construct_labeled_default_then_unlabeled() {
        val ttype = tokenType()
        assertFailsWith<NiwenLexerException> {
            niwenLexer {
                default state {
                    "." isToken ttype
                }
                state {
                    "." isToken ttype
                }
            }
        }
    }

    @Test
    fun fails_to_construct_multiple_unlabeled_states() {
        val ttype = tokenType()
        assertFailsWith<NiwenLexerException> {
            niwenLexer {
                state {
                    "." isToken ttype
                }
                state {
                    "." isToken ttype
                }
            }
        }
    }

    @Test
    fun fails_to_construct_multiple_default_states() {
        val ttype = tokenType()
        assertFailsWith<NiwenLexerException> {
            niwenLexer {
                default state {
                    "." isToken ttype
                }
                default state {
                    "." isToken ttype
                }
            }
        }
    }

    @Test
    fun cannot_create_two_states_with_the_same_label() {
        val a = stateLabel()
        val ta = tokenType()
        val tb = tokenType()
        val exc = assertFailsWith<NiwenLexerException> {
            niwenLexer {
                default state {
                    "ab" isToken tb
                }
                a state {
                    "b" isToken tb
                }
                a state {
                    "a" isToken ta
                }
            }
        }
        assertTrue(exc.message!!.contains("two states with the same label"))
    }

    @Test
    fun successfully_constructs_multiple_states_and_starts_on_default() {
        val one = tokenType()
        val two = tokenType()
        val other = stateLabel()

        val lexer = niwenLexer {
            default state {
                "1" isToken one
            }
            other state {
                "2" isToken two
            }
        }
        assertEquals(2, lexer.statesCount)

        // Check contents of default state
        val defState = lexer.defaultState
        assertEquals(1, defState.matchers.size)
        val oneMatcher =
            (defState.matchers[0] as? TokenRecognizerMatched)?.recognizer as? StringRecognizer
                ?: error("Incorrect matcher type")
        assertEquals("1", oneMatcher.toRecognize)

        // Check contents of other state
        val oState = lexer.getState(other)
        assertEquals(1, oState.matchers.size)
        val twoMatcher =
            (oState.matchers[0] as? TokenRecognizerMatched)?.recognizer as? StringRecognizer
                ?: error("Incorrect matcher type")
        assertEquals("2", twoMatcher.toRecognize)

        // Check that first state is used
        val result = lexer.tokenize("1")
        assertEquals(listOf(Token("1", 0, 1, one)), result)

        // Also check that trying to match thing from second state fails
        assertFailsWith<NiwenLexerNoMatchException> {
            lexer.tokenize("12")
        }
    }

    @Test
    fun supports_switching_from_state_to_state() {
        val one = tokenType()
        val two = tokenType()
        val other = stateLabel()

        val lexer = niwenLexer {
            default state {
                "1" isToken one thenState other
            }
            other state {
                "2" isToken two thenState default
            }
        }
        val result = lexer.tokenize("1212")
        assertEquals(
            listOf(
                Token("1", 0, 1, one),
                Token("2", 1, 2, two),
                Token("1", 2, 3, one),
                Token("2", 3, 4, two)
            ),
            result
        )
    }

    @Test
    fun supports_redirecting_the_default_state() {
        val a = stateLabel()
        val b = stateLabel()
        val ta = tokenType()
        val tb = tokenType()
        val lexer = niwenLexer {
            default state a
            a state {
                "a" isToken ta thenState b
            }
            b state {
                "b" isToken tb thenState a
                "c" isToken tb thenState default // should also work
            }
        }
        val string = "abacaba"
        val expected = listOf(
            Token("a", 0, 1, ta),
            Token("b", 1, 2, tb),
            Token("a", 2, 3, ta),
            Token("c", 3, 4, tb),
            Token("a", 4, 5, ta),
            Token("b", 5, 6, tb),
            Token("a", 6, 7, ta)
        )
        val actual = lexer.tokenize(string)
        assertEquals(expected, actual)
    }

    @Test
    fun cannot_redefine_default_after_redirecting_default() {
        val a = stateLabel()
        val ta = tokenType()
        val b = stateLabel()
        val tb = tokenType()
        val exc = assertFailsWith<NiwenLexerException> {
            niwenLexer {
                default state a
                default state {
                    "a" isToken ta
                }
                b state {
                    "b" isToken tb
                }
            }
        }
        assertTrue(exc.message!!.contains("already defined"))
    }

    @Test
    fun cannot_redefine_default_state_in_single_state_lexer_kind() {
        val a = stateLabel()
        val ta = tokenType()
        val exc = assertFailsWith<NiwenLexerException> {
            niwenLexer {
                state {
                    "a" isToken ta
                }
                default state a
            }
        }
        assertTrue(exc.message!!.contains("Cannot redefine"))
        assertTrue(exc.message!!.contains("single-state"))
    }
}