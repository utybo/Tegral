package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.parser.dsl.emit
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import kotlin.test.Test
import kotlin.test.assertEquals

class EmitDslTest {
    data class Value(val value: Int) {
        companion object : ParserNodeDeclaration<Value> by reflective()
    }

    @Test
    fun `Simple emit test`() {
        val parser = niwenParser {
            Value root {
                emit(123) storeIn Value::value
            }
        }
        val res = parser.parse(listOf())
        assertEquals(123, res.value)
    }
}
