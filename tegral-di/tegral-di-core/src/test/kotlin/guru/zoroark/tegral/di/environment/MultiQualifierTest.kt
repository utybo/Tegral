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

package guru.zoroark.tegral.di.environment

import guru.zoroark.tegral.di.InvalidDeclarationException
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class MultiQualifierTest {

    data class ToStringPassthrough(private val value: String) : Qualifier {
        override fun toString(): String {
            return value
        }
    }

    @Test
    fun `toString, multiple elements`() {
        val qualifier = MultiQualifier(setOf(ToStringPassthrough("hello"), ToStringPassthrough("world")))
        assertEquals("hello + world", qualifier.toString())
    }

    @Test
    fun `init, fails on EmptyQualifier`() {
        val ex = assertThrows<InvalidDeclarationException> {
            MultiQualifier(setOf(named("hello"), EmptyQualifier, named("goodbye")))
        }
        val message = assertNotNull(ex.message)
        assertContains(message, "must not contain EmptyQualifier")
    }

    @Test
    fun `init, fails on MultiQualifier`() {
        val first = MultiQualifier(setOf(named("a"), named("b")))
        val ex = assertThrows<InvalidDeclarationException> {
            MultiQualifier(setOf(named("c"), first))
        }
        val message = assertNotNull(ex.message)
        assertContains(message, "cannot be nested")
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1])
    fun `init, fails on less than two components`(size: Int) {
        val qualifiers = List(size) { named("a") }.toSet()
        val ex = assertThrows<InvalidDeclarationException> {
            MultiQualifier(qualifiers)
        }
        val message = assertNotNull(ex.message)
        assertContains(message, "less than 2 qualifiers")
    }

    @Test
    fun `plus, empty empty`() {
        assertEquals(EmptyQualifier, EmptyQualifier + EmptyQualifier)
    }

    @Test
    fun `plus, empty non-empty`() {
        val qualifier = named("test")
        assertSame(qualifier, EmptyQualifier + qualifier)
        assertSame(qualifier, qualifier + EmptyQualifier)
    }

    @Test
    fun `plus, non-empty non-empty`() {
        assertEquals(MultiQualifier(setOf(named("a"), named("b"))), named("a") + named("b"))
    }

    @Test
    fun `plus, multi non-empty`() {
        assertEquals(
            MultiQualifier(setOf(named("a"), named("b"), named("c"))),
            MultiQualifier(setOf(named("a"), named("b"))) + named("c")
        )
    }

    @Test
    fun `plus, non-empty multi`() {
        assertEquals(
            MultiQualifier(setOf(named("a"), named("b"), named("c"))),
            named("c") + MultiQualifier(setOf(named("a"), named("b")))
        )
    }

    @Test
    fun `plus, multi multi`() {
        assertEquals(
            MultiQualifier(setOf(named("a"), named("b"), named("c"), named("d"))),
            MultiQualifier(setOf(named("c"), named("d"))) + MultiQualifier(setOf(named("a"), named("b")))
        )
    }

    @Test
    fun `equals, wrong type`() {
        assertNotEquals(MultiQualifier(setOf(named("a"), named("b"))), Any())
    }

    @Test
    fun `equals, wrong qualifiers`() {
        assertNotEquals(
            MultiQualifier(setOf(named("a"), named("b"))),
            MultiQualifier(setOf(named("a"), named("c")))
        )
    }
}
