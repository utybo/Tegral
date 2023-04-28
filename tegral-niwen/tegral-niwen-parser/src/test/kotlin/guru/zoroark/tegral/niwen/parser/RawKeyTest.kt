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

import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.item
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.repeated
import guru.zoroark.tegral.niwen.parser.expectations.RawKey
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

class RawKeyTest {

    data class Word(val characters: List<String>) {
        companion object : ParserNodeDeclaration<Word> by reflective()
    }

    @Test
    fun `Store in raw key then retrieve`() {
        val tChar = tokenType("character")
        val lexer = niwenLexer {
            state {
                'a'..'z' isToken tChar
            }
        }
        val parser = niwenParser<Word> {
            Word root {
                repeated {
                    expect(tChar) storeIn item
                } storeIn RawKey(typeOf<List<String>>(), "characters")
            }
        }
        val word = parser.parse(lexer.tokenize("abcd"))
        assertEquals(Word(listOf("a", "b", "c", "d")), word)
    }
}
