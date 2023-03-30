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
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NiwenLexerTest {

    @Test
    fun empty_should_crash() {
        // I mean, yeah, that lexer is not going to do anything
        assertFailsWith<NiwenLexerException> {
            niwenLexer {}
        }
    }

    @Test
    fun constructs_single_unlabeled_state() {
        // Should construct a single empty state
        val ret = niwenLexer {
            state {}
        }

        assertEquals(ret.statesCount, 1)
        assertTrue(ret.defaultState.matchers.isEmpty())
    }

    @Test
    fun is_able_to_lex_simple_unlabeled_state() {
        // Should construct a single state with a single matcher
        val simpleStateDot =
            tokenType()
        val lexer = niwenLexer {
            state {
                "." isToken simpleStateDot
            }
        }
        val tokens = lexer.tokenize("....")
        assertEquals(lexer.statesCount, 1)
        assertEquals(lexer.defaultState.matchers.size, 1)
        assertEquals(
            tokens,
            (0 until 4).map { i ->
                Token(
                    string = ".", startsAt = i, endsAt = i + 1,
                    tokenType = simpleStateDot
                )
            }
        )
    }

    @Test
    fun is_able_to_lex_multiple_token_types_unlabeled_state() {
        // Should successfully lex with a single more complex state
        val ttdot = tokenType()
        val ttspace = tokenType()
        val tthello = tokenType()
        val lexer = niwenLexer {
            state {
                "." isToken ttdot
                " " isToken ttspace
                "hello" isToken tthello
            }
        }
        val tokens = lexer.tokenize("hello hello. hello..  ")
        assertEquals(
            tokens,
            listOf(
                Token("hello", 0, 5, tthello),
                Token(" ", 5, 6, ttspace),
                Token("hello", 6, 11, tthello),
                Token(".", 11, 12, ttdot),
                Token(" ", 12, 13, ttspace),
                Token("hello", 13, 18, tthello),
                Token(".", 18, 19, ttdot),
                Token(".", 19, 20, ttdot),
                Token(" ", 20, 21, ttspace),
                Token(" ", 21, 22, ttspace)
            )
        )
    }

    @Test
    fun is_able_to_parse_some_funny_string_patterns_v2() {
        // Additional testing, specifically because lexing tests are supposed
        // to be done sequentially (i.e. check for the first pattern, then the
        // second, etc.)
        val tttriple = tokenType()
        val ttpair = tokenType()
        val ttsingle = tokenType()
        val ttspace = tokenType()
        val lexer = niwenLexer {
            state {
                "..." isToken tttriple
                ".." isToken ttpair
                "." isToken ttsingle
                " " isToken ttspace
            }
        }
        //                           111223445666789
        val tokens = lexer.tokenize("..... .. .... .")
        assertEquals(
            tokens,
            listOf(
                Token("...", 0, 3, tttriple), // 1
                Token("..", 3, 5, ttpair), // 2
                Token(" ", 5, 6, ttspace), // 3
                Token("..", 6, 8, ttpair), // 4
                Token(" ", 8, 9, ttspace), // 5
                Token("...", 9, 12, tttriple), // 6
                Token(".", 12, 13, ttsingle), // 7
                Token(" ", 13, 14, ttspace), // 8
                Token(".", 14, 15, ttsingle) // 9
            )
        )
    }

    @Test
    fun supports_custom_matchers() {
        // ttype will be the type returned by our custom matcher
        val ttype = tokenType()
        val ttdot = tokenType()
        val customMatcher = matcher { s, i ->
            if (s[i] == 'e') {
                Token("e", i, i + 1, ttype)
            } else {
                null
            }
        }
        val lexer = niwenLexer {
            state {
                +customMatcher
                "." isToken ttdot
            }
        }

        val tokens = lexer.tokenize(".e..ee")
        assertEquals(
            tokens,
            listOf(
                Token(".", 0, 1, ttdot),
                Token("e", 1, 2, ttype),
                Token(".", 2, 3, ttdot),
                Token(".", 3, 4, ttdot),
                Token("e", 4, 5, ttype),
                Token("e", 5, 6, ttype)
            )
        )
    }

    @Test
    fun incoherent_matcher_results_cause_exception_start_before_index() {
        // Our token types
        val ttype = tokenType()
        val ttdot = tokenType()
        val lexer = niwenLexer {
            state {
                // Erroneous matcher
                +matcher { s, start ->
                    if (start == 1)
                    // The second character returns a token that starts
                    // on the very first character, which is a big no-no
                        Token(s[0].toString(), 0, 2, ttype)
                    else
                    // Returning null to signal no match
                        null
                }
                "." isToken ttdot
            }
        }
        val exc = assertFailsWith<NiwenLexerException> {
            lexer.tokenize("...")
        }
        assertNotNull(exc.message)
        assertTrue(exc.message!!.contains("token starts"))
    }

    @Test
    fun incoherent_matcher_results_cause_exception_end_is_too_far() {
        val ttype = tokenType()
        val ttdot = tokenType()
        val lexer = niwenLexer {
            state {
                +matcher { s, start ->
                    if (start == s.length - 1) {
                        Token(
                            s[start].toString(),
                            s.length - 1,
                            s.length + 1,
                            ttype
                        )
                    } else {
                        null
                    }
                }
                "." isToken ttdot
            }
        }
        val exc = assertFailsWith<NiwenLexerException> {
            lexer.tokenize("....")
        }
        assertNotNull(exc.message)
        assertTrue(exc.message!!.contains("token ends"))
    }

    @Test
    fun no_match_fails() {
        val ttdot = tokenType()
        val lexer = niwenLexer {
            state {
                "." isToken ttdot
            }
        }
        assertFailsWith<NiwenLexerNoMatchException> {
            lexer.tokenize("a")
        }
    }

    @Test
    fun supports_regex() {
        val ttregex = tokenType()
        val lexer = niwenLexer {
            state {
                matches("(abc){2}") isToken ttregex
            }
        }
        val result = lexer.tokenize("abcabcabcabc")
        assertEquals(
            listOf(
                Token("abcabc", 0, 6, ttregex),
                Token("abcabc", 6, 12, ttregex)
            ),
            result
        )
    }

    @Test
    fun supports_transparent_lookbehind_in_regex() {
        val ttregex = tokenType()
        val ttype = tokenType()
        val lexer = niwenLexer {
            state {
                "a" isToken ttype
                matches("(?<=a)b") isToken ttregex
                "b" isToken ttype
            }
        }
        val result = lexer.tokenize("abb")
        assertEquals(
            listOf(
                Token("a", 0, 1, ttype),
                Token("b", 1, 2, ttregex),
                Token("b", 2, 3, ttype)
            ),
            result
        )
    }

    @Test
    fun regex_matches_start_and_end_of_string_as_real_start_and_end() {
        val ttregex = tokenType()
        val ttype = tokenType()
        val lexer = niwenLexer {
            state {
                matches("^a") isToken ttregex
                "a" isToken ttype
                matches("b$") isToken ttregex
                "b" isToken ttype
            }
        }
        val result = lexer.tokenize("aabb")
        assertEquals(
            listOf(
                Token("a", 0, 1, ttregex),
                Token("a", 1, 2, ttype),
                Token("b", 2, 3, ttype),
                Token("b", 3, 4, ttregex)
            ),
            result
        )
    }

    @Test
    fun anyOf_crashes_if_no_provided_arguments() {
        val tokenType =
            tokenType()
        val exc = assertFailsWith<NiwenLexerException> {
            niwenLexer {
                state {
                    anyOf() isToken tokenType
                }
            }
        }
        assertNotNull(exc.message)
        assertTrue(exc.message!!.contains("anyOf") && exc.message!!.contains("at least one"))
    }

    @Test
    fun supports_anyOf_multistring_matcher() {
        val basicTokenType =
            tokenType()
        val multiTokenType =
            tokenType()
        val lexer = niwenLexer {
            state {
                " " isToken basicTokenType
                anyOf("a", "b", "d", "z") isToken multiTokenType
            }
        }
        val tokens = lexer.tokenize("a bz d bdz")
        assertEquals(
            listOf(
                Token("a", 0, 1, multiTokenType),
                Token(" ", 1, 2, basicTokenType),
                Token("b", 2, 3, multiTokenType),
                Token("z", 3, 4, multiTokenType),
                Token(" ", 4, 5, basicTokenType),
                Token("d", 5, 6, multiTokenType),
                Token(" ", 6, 7, basicTokenType),
                Token("b", 7, 8, multiTokenType),
                Token("d", 8, 9, multiTokenType),
                Token("z", 9, 10, multiTokenType)
            ),
            tokens
        )
    }
}
