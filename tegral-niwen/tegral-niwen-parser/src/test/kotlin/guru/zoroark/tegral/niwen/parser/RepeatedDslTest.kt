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
import guru.zoroark.tegral.niwen.lexer.stateLabel
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.either
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.item
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.or
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
                repeated { expect(Value) storeIn item } storeIn ValueRow::expressions
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

    @Test
    fun `Does not emit items`() {
        // This test stores even characters only
        val sEven = stateLabel()
        val tOdd = tokenType("charOdd")
        val tEven = tokenType("charEven")
        val lexer = niwenLexer {
            default state {
                'a'..'z' isToken tOdd thenState sEven
            }

            sEven state {
                ('a'..'z') isToken tEven thenState default
            }
        }

        val parser = niwenParser<Value> {
            Value root {
                repeated {
                    either {
                        expect(tOdd)
                    } or {
                        expect(tEven) storeIn item
                    }
                } transform { it.joinToString(separator = "") } storeIn Value::value
            }
        }

        val result = parser.parse(lexer.tokenize("abcdefghijk"))
        assertEquals(Value("bdfhj"), result)
    }
}
