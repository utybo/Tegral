package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.Token
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.emit
import guru.zoroark.tegral.niwen.parser.dsl.expectEof
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EofDslTest {
    data class Value(val value: Int) {
        companion object : ParserNodeDeclaration<Value> by reflective()
    }

    @Test
    fun `Matching eof`() {
        val parser = niwenParser {
            Value root {
                expectEof()
                emit(123) storeIn Value::value
            }
        }
        val res = parser.parse(listOf())
        assertEquals(123, res.value)
    }

    @Test
    fun `Not matching eof`() {
        val parser = niwenParser {
            Value root {
                expectEof()
                emit(123) storeIn Value::value
            }
        }
        val exc = assertThrows<NiwenParserException> {
            parser.parse(listOf(Token("a", 0, 1, tokenType())))
        }
        val message = exc.message
        assertNotNull(message)
        assertContains(message, "Did not reach end of input, was at index 0 while there is/are 1 token(s)")
    }
}
