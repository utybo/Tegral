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

package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DslTest {

    data class NumberNode(val value: String) {
        companion object : ParserNodeDeclaration<NumberNode> by reflective()
    }

    data class AdditionNode(val first: NumberNode, val second: NumberNode) {
        companion object : ParserNodeDeclaration<AdditionNode> by reflective()
    }

    @Test
    fun addition_parser_DSL_test() {
        val tokenNumber = tokenType()
        val tokenPlus = tokenType()
        val lexer = niwenLexer {
            state {
                matches("\\d+") isToken tokenNumber
                "+" isToken tokenPlus
                " ".ignore
            }
        }
        val parser = niwenParser {
            NumberNode {
                expect(tokenNumber) storeIn NumberNode::value
            }
            AdditionNode root {
                expect(NumberNode) storeIn AdditionNode::first
                expect(tokenPlus)
                expect(NumberNode) storeIn AdditionNode::second
            }
        }
        val tokens = lexer.tokenize("123 + 4567")
        val ast = parser.parse(tokens)
        assertEquals(
            AdditionNode(
                NumberNode("123"),
                NumberNode("4567")
            ),
            ast
        )
    }

    @Test
    fun cannot_declare_same_type_twice() {
        val token = tokenType()
        assertFailsWith<NiwenParserException> {
            niwenParser<Nothing> {
                NumberNode {
                    expect(token)
                }
                NumberNode {
                    error("Should not be called")
                }
            }
        }.apply {
            val msg = message
            assertNotNull(msg)
            assertTrue(msg.contains("NumberNode") && msg.contains("already"))
        }
    }

    @Test
    fun fail_if_root_not_declared() {
        val token = tokenType()
        assertFailsWith<NiwenParserException> {
            niwenParser<Nothing> {
                AdditionNode {
                    expect(token)
                }
                NumberNode {
                    expect(token)
                }
            }
        }.apply {
            val msg = message
            assertNotNull(msg)
            assertTrue(msg.contains("root") && msg.contains("never"))
        }
    }

    @Test
    fun `Parser not consuming all tokens`() {
        val token = tokenType()
        val lexer = niwenLexer {
            state { 'a'..'z' isToken token }
        }
        val parser = niwenParser {
            ParserTest.LiterallyAString root {
                expect(token) storeIn ParserTest.LiterallyAString::value
            }
        }
        val result = parser.parseWithDebugger(lexer.tokenize("ab"))
        assertIs<NiwenParser.ParserResult.Failure<ParserTest.LiterallyAString>>(result)
        assertEquals(
            "Parsing stopped, but not all tokens were consumed. Stopped at index 1 while there are 2 tokens",
            result.reason
        )
    }

    @Test
    fun `Parser with two root nodes defined`() {
        val token = tokenType()
        val exc = assertFailsWith<NiwenParserException> {
            niwenParser {
                ParserTest.LiterallyAString root {
                    expect(token) storeIn ParserTest.LiterallyAString::value
                }

                ParserTest.LiterallyAString root {
                    expect(token) storeIn ParserTest.LiterallyAString::value
                }
            }
        }
        assertEquals(
            exc.message,
            "Another node was already defined as the root, class " +
                "guru.zoroark.tegral.niwen.parser.dsl.NiwenParserBuilder cannot also be a root."
        )
    }

    @Test
    fun `Parser with a group`() {
        val tokenNumber = tokenType()
        val tokenPlus = tokenType()
        val lexer = niwenLexer {
            state {
                matches("\\d+") isToken tokenNumber
                "+" isToken tokenPlus
                " ".ignore
            }
        }
        val parser = niwenParser {
            "numbers" group {
                NumberNode {
                    expect(tokenNumber) storeIn NumberNode::value
                }
            }
            "addition" group {
                AdditionNode root {
                    expect(NumberNode) storeIn AdditionNode::first
                    expect(tokenPlus)
                    expect(NumberNode) storeIn AdditionNode::second
                }
            }
        }
        val tokens = lexer.tokenize("123 + 4567")
        val ast = parser.parse(tokens)
        assertEquals(
            AdditionNode(
                NumberNode("123"),
                NumberNode("4567")
            ),
            ast
        )
    }
}
