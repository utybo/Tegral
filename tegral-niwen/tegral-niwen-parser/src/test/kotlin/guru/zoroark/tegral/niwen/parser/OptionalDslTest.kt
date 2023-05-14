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

import guru.zoroark.tegral.niwen.lexer.matchers.repeated
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.optional
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
