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

import guru.zoroark.tegral.di.ComponentNotFoundException
import guru.zoroark.tegral.di.ElementClass
import guru.zoroark.tegral.di.FakeComponent
import guru.zoroark.tegral.di.OtherElementClass
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.extensions.EagerImmutableMetaEnvironment
import guru.zoroark.tegral.di.test.entryOf
import guru.zoroark.tegral.di.test.environment.NotExtensibleEnvironmentBaseTest
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestEagerEnvironment : NotExtensibleEnvironmentBaseTest(::EagerImmutableMetaEnvironment) {
    @Test
    fun `Base tests`() {
        runTests()
    }

    @Test
    fun `Test object injection is eager`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { ElementClass() },
                entryOf { OtherElementClass() }
            )
        )
        val env = EagerImmutableMetaEnvironment(context)
        var wasInjected = false
        val inj = env.createInjector(Identifier(ElementClass::class)) { wasInjected = true }
        assertTrue(wasInjected)
        inj.getValue(FakeComponent, FakeComponent::fakeProperty)
        assertTrue(wasInjected)
    }

    @Test
    fun `Test object injection after creation and does not exist throws`() {
        val context = EnvironmentContext(mapOf(entryOf { OtherElementClass() }))
        val env = EagerImmutableMetaEnvironment(context)
        val ex = assertThrows<ComponentNotFoundException> {
            env.createInjector(Identifier(ElementClass::class))
        }
        assertEquals(Identifier(ElementClass::class), ex.notFound)
    }

    @Test
    fun `Simple creation via DSL`() {
        val env = tegralDi(EagerImmutableMetaEnvironment) {
            put { "Hello" }
        }
        assertEquals("Hello", env.get())
    }
}
