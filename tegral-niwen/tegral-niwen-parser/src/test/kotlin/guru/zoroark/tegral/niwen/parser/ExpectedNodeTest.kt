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

import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.StoreStateCallback
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExpectedNodeTest {
    @Suppress("UtilityClassWithPublicConstructor")
    class One {
        companion object : ParserNodeDeclaration<One> by reflective()
    }

    @Suppress("UtilityClassWithPublicConstructor")
    class Two {
        companion object : ParserNodeDeclaration<Two> by reflective()
    }

    @Test
    fun test_undefined_expected_node_fails() {
        val exp = ExpectedNode<Nothing, Two>(Two)
        assertFailsWith<NiwenParserException> {
            exp.matches(
                ParsingContext(
                    listOf(),
                    mapOf(One to DescribedType(One, listOf()))
                ),
                0
            )
        }.apply {
            val msg = message
            assertNotNull(msg)
            assertTrue(
                msg.contains("Two") && msg.contains("declared"),
                "Expected 'Two' and 'declared' in exception message $msg"
            )
        }
    }

    @Test
    fun test_successful() {
        val key = NodeParameterKey<Nothing, Two>(typeOf<Two>(), "yeet")
        val exp = ExpectedNode(Two, stateCallback = StoreStateCallback(key))
        val res = exp.matches(
            ParsingContext(
                listOf(),
                mapOf(Two to DescribedType(Two, listOf()))
            ),
            0
        )
        assertTrue(res is ExpectationResult.Success)
        assertTrue(res.stored[key] is Two)
    }
}
