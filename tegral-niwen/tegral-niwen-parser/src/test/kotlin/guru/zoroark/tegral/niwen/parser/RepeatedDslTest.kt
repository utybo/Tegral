package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.item
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.repeated
import kotlin.test.Test
import kotlin.test.assertEquals

class RepeatedDslTest {
    data class ValueRow(val indicator: String, val expressions: List<Value>) {
        companion object : ParserNodeDeclaration<ValueRow> by reflective()
    }

    data class Value(val value: String) {
        companion object : ParserNodeDeclaration<Value> by reflective()
    }

    @Test
    fun `Single item`() {
        val word = tokenType("word")
        val colon = tokenType("colon")
        val lexer = niwenLexer {
            default state {
                matches("[a-zA-Z]+") isToken word
                ":" isToken colon
                " ".ignore
            }
        }
        val parser = niwenParser<ValueRow> {
            ValueRow root {
                expect(word) storeIn ValueRow::indicator
                expect(colon)
                repeated{ expect(Value) storeIn item } storeIn ValueRow::expressions
            }

            Value {
                expect(word) storeIn Value::value
            }
        }
        val input = "Hey: One Two Three"
        val tokens = lexer.tokenize(input)
        val result = parser.parse(tokens)
        assertEquals(
            result,
            ValueRow(
                "Hey",
                listOf(Value("One"), Value("Two"), Value("Three"))
            )
        )
    }
}