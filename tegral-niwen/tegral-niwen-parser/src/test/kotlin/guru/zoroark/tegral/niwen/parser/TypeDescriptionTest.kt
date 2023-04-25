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

import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TypeDescriptionTest {
    private val dummyOneKey = NodeParameterKey<Nothing, String>(typeOf<String>(), "One")

    @Test
    fun argument_retrieval_works() {
        val ptd = TypeDescription(mapOf(dummyOneKey to "Two"))
        val str: String = ptd[dummyOneKey]
        assertEquals("Two", str)
    }

    @Test
    fun argument_retrieval_wrong_type_fails() {
        val ptd = TypeDescription(mapOf(dummyOneKey to "Two"))
        assertFailsWith<NiwenParserException> {
            val str: Int = ptd[NodeParameterKey<Nothing, Int>(typeOf<Int>(), "One")]
            println("But $str is not an integer!") // Should not happen
        }.apply {
            val msg = message
            assertNotNull(msg)
        }
    }

    @Test
    fun argument_retrieval_key_absent_fails() {
        val ptd = TypeDescription(mapOf(dummyOneKey to "Two"))
        assertFailsWith<NiwenParserException> {
            val str: String = ptd[NodeParameterKey(typeOf<String>(), "Three")]
            println("But $str does not exist!") // Should not happen
        }.apply {
            val msg = message
            assertNotNull(msg)
            assertTrue(msg.contains("does not exist") && msg.contains("Three"))
        }
    }
}
