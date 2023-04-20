package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import kotlin.test.Test
import kotlin.test.assertEquals

class ComplexStateExtractionTest {
    @Test
    fun `Transform then store`() {
        val numberToken = tokenType("numberToken")
        val lexer = niwenLexer {
            default state {
                matches("[0-9]+") isToken numberToken
            }
        }
        val parser = niwenParser {
            IntHolder root {
                expect(numberToken) transform { it.toInt() } storeIn IntHolder::int
            }
        }
        val result = parser.parse(lexer.tokenize("1010"))
        assertEquals(1010, result.int)
    }

    @Test
    fun `Transform twice then store`() {
        val numberToken = tokenType("numberToken")
        val lexer = niwenLexer {
            default state {
                matches("[0-9]+") isToken numberToken
            }
        }
        val parser = niwenParser {
            StringHolder root {
                expect(numberToken) transform { it.toInt() } transform { it.toString() } storeIn StringHolder::str
            }
        }
        val result = parser.parse(lexer.tokenize("1010"))
        assertEquals("1010", result.str)
    }


    data class IntHolder(val int: Int) {
        companion object : ParserNodeDeclaration<IntHolder> by reflective()
    }

    data class StringHolder(val str: String) {
        companion object : ParserNodeDeclaration<StringHolder> by reflective()
    }
}