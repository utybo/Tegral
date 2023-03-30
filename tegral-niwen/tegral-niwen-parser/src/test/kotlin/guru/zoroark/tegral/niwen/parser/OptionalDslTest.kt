package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.matchers.repeated
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.optional
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import kotlin.test.Test
import kotlin.test.assertEquals


class OptionalDslTest {

    data class Decimal(val intPart: String, val decPart: String? = null) {
        companion object : ParserNodeDeclaration<Decimal> by reflective()
    }

    @Test
    fun test_optional_dsl() {
        val tNum = tokenType()
        val tDot = tokenType()
        val lexer = niwenLexer {
            state {
                ('0'..'9').repeated isToken tNum
                "." isToken tDot
            }
        }
        val parser = niwenParser {
            Decimal root {
                expect(tNum) storeIn Decimal::intPart
                optional {
                    expect(tDot)
                    expect(tNum) storeIn Decimal::decPart
                }
            }
        }
        val result = parser.parse(lexer.tokenize("123.456"))
        assertEquals(Decimal("123", "456"), result)

        val result2 = parser.parse(lexer.tokenize("789"))
        assertEquals(Decimal("789"), result2)
    }

}