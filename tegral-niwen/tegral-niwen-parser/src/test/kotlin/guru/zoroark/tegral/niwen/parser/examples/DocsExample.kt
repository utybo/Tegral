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

package guru.zoroark.tegral.niwen.parser.examples

import guru.zoroark.tegral.niwen.lexer.TokenType
import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.matchers.repeated
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.dsl.either
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.expectEof
import guru.zoroark.tegral.niwen.parser.dsl.item
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.or
import guru.zoroark.tegral.niwen.parser.dsl.repeated
import guru.zoroark.tegral.niwen.parser.reflective
import kotlin.test.Test
import kotlin.test.assertEquals

class DocsExample {
    // First, we define our types:
    data class Sum(val left: Number, val right: Number) {
        companion object : ParserNodeDeclaration<Sum> by reflective()
    }

    data class Number(val value: Int) {
        companion object : ParserNodeDeclaration<Number> by reflective()
    }

    data class SumsDocument(val sums: List<Sum>) {
        companion object : ParserNodeDeclaration<SumsDocument> by reflective()
    }

// Then, our lexer with its tokens:

    enum class Tokens : TokenType {
        NUMBER,
        PLUS,
        NEWLINE
    }

    val lexer = niwenLexer {
        state {
            matches("\\d+") isToken Tokens.NUMBER
            '+' isToken Tokens.PLUS
            '\n'.repeated isToken Tokens.NEWLINE
            ' '.ignore
        }
    }

// And our parser:

    val parser = niwenParser<SumsDocument> {
        SumsDocument root {
            repeated {
                expect(Sum) storeIn item
            } storeIn SumsDocument::sums
        }

        Sum {
            expect(Number) storeIn Sum::left
            expect(Tokens.PLUS)
            expect(Number) storeIn Sum::right
            either {
                expect(Tokens.NEWLINE)
            } or {
                expectEof()
            }
        }

        Number {
            expect(Tokens.NUMBER) transform { it.toInt() } storeIn Number::value
        }
    }

    @Test
    fun `Run parser`() {
        val tokens = lexer.tokenize(
            """
            1 + 2
            33     +       44
            
            
            555+666
            """.trimIndent()
        )
        val result = parser.parse(tokens)
        println(result)
        assertEquals(
            SumsDocument(
                sums = listOf(
                    Sum(Number(1), Number(2)),
                    Sum(Number(33), Number(44)),
                    Sum(Number(555), Number(666))
                )
            ),
            result
        )
    }
    /*
     * SumsDocument(
     *   sums=[
     *     Sum(left=Number(value=1), right=Number(value=2)),
     *     Sum(left=Number(value=33), right=Number(value=44)),
     *     Sum(left=Number(value=555), right=Number(value=666))
     *   ]
     * )
     */
}
