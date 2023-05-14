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
import guru.zoroark.tegral.niwen.parser.expectations.asKey
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestReflectiveNodeDeclaration {
    data class SingleConstructor(val hello: String, val goodbye: Int) {
        companion object :
            ParserNodeDeclaration<SingleConstructor> by reflective()
    }

    @Test
    fun reflective_node_declaration_works_single_ctor() {
        val sc = SingleConstructor.make(
            TypeDescription(mapOf(SingleConstructor::hello.asKey() to "HI", SingleConstructor::goodbye.asKey() to 42))
        )
        assertEquals("HI", sc.hello)
        assertEquals(42, sc.goodbye)
    }

    data class DoubleConstructor(val hello: String, val goodbye: Int) {
        companion object :
            ParserNodeDeclaration<DoubleConstructor> by reflective()

        @Suppress("unused")
        constructor(ciao: Int, buongiorno: String) : this(buongiorno, ciao)
    }

    @Test
    fun reflective_node_declaration_works_double_ctor() {
        val sc = DoubleConstructor.make(
            TypeDescription(
                mapOf(
                    NodeParameterKey<DoubleConstructor, String>(typeOf<String>(), "buongiorno") to "ARRIVEDERCI",
                    NodeParameterKey<DoubleConstructor, Int>(typeOf<Int>(), "ciao") to 765
                )
            )
        )
        assertEquals("ARRIVEDERCI", sc.hello)
        assertEquals(765, sc.goodbye)
    }

    @Test
    fun reflective_node_declaration_fails_on_no_match() {
        assertFailsWith<NiwenParserException> {
            DoubleConstructor.make(
                TypeDescription(
                    mapOf(
                        NodeParameterKey<DoubleConstructor, String>(typeOf<String>(), "buongiorno") to "ARRIVEDERCI",
                        NodeParameterKey<DoubleConstructor, Int>(typeOf<Int>(), "goodbye") to 42
                    )
                )
            )
        }.apply {
            val msg = message
            assertNotNull(msg)
            assertTrue(msg.contains("Could not find"))
        }
    }

    data class OptionalConstructor(
        val first: String,
        val e: Char,
        val optSecond: Int = 42
    ) {
        companion object :
            ParserNodeDeclaration<OptionalConstructor> by reflective()
    }

    @Test
    fun reflective_node_declaration_with_optional_params() {
        val result = OptionalConstructor.make(
            TypeDescription(
                mapOf(
                    OptionalConstructor::first.asKey() to "Hello",
                    OptionalConstructor::e.asKey() to 'E'
                )
            )
        )
        assertEquals(OptionalConstructor("Hello", 'E'), result)
    }

    data class MultiOptionalConstructor(
        val first: String,
        val second: Char,
        val third: List<String> = listOf()
    ) {
        constructor(
            second: Char,
            first: String = "Heyy",
            @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER")
            third: List<String> = listOf()
        ) : this("Second ctor $first", second, listOf("Hello"))

        companion object :
            ParserNodeDeclaration<MultiOptionalConstructor> by reflective()
    }

    @Test
    fun reflective_node_declaration_prefer_least_optional() {
        val result = MultiOptionalConstructor.make(
            TypeDescription(
                mapOf(
                    MultiOptionalConstructor::first.asKey() to "HELLO",
                    MultiOptionalConstructor::second.asKey() to 'X'
                )
            )
        )
        assertEquals(MultiOptionalConstructor("HELLO", 'X', listOf()), result)
    }
}
