package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
}