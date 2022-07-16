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
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class FullTypeQualifierTest {
    class Container<T> {
        var value: T? = null
    }

    @Test
    fun `Test full type qualifier can inject two similarly typed`() {
        val env = tegralDi {
            put(typed<Container<List<String>>>()) { Container<List<String>>() }
            put(typed<Container<List<Int>>>()) { Container<List<Int>>() }
        }
        val strListContainer = env.get<Container<List<String>>>(typed<Container<List<String>>>())
        val intListContainer = env.get<Container<List<Int>>>(typed<Container<List<Int>>>())
        assertNotSame<Container<*>>(strListContainer, intListContainer)
    }

    @Test
    fun `Test full type qualifier fails with exact same type`() {
        assertThrows<InvalidDeclarationException> {
            tegralDi {
                put(typed<Container<List<String>>>()) { Container<List<String>>() }
                put(typed(typeOf<Container<List<String>>>())) { Container<List<String>>() }
            }
        }
    }

    @Test
    fun `Test full type qualifier does not fail with type projection`() {
        val env = tegralDi {
            put(typed<Container<List<String>>>()) { Container<List<String>>() }
            put(typed<Container<List<*>>>()) { Container<List<String>>() }
        }
        val strListContainer = env.get<Container<List<String>>>(typed<Container<List<String>>>())
        val projectedListContainer = env.get<Container<List<*>>>(typed<Container<List<*>>>())
        assertNotSame<Container<*>>(strListContainer, projectedListContainer)
    }

    @Test
    fun `typed DSL creates corresponding qualifier`() {
        val type = typeOf<List<String>>()
        val qualifier = typed(type)
        assertEquals(type, qualifier.type)
    }
}
