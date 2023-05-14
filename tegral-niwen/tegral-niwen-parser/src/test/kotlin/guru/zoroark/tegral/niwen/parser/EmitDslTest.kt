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
