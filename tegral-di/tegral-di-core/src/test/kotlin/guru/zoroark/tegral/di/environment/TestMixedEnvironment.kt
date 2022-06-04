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

import guru.zoroark.tegral.di.ElementClass
import guru.zoroark.tegral.di.FakeComponent
import guru.zoroark.tegral.di.OtherElementClass
import guru.zoroark.tegral.di.extensions.ExtensibleEnvironmentContext
import guru.zoroark.tegral.di.test.entryOf
import guru.zoroark.tegral.di.test.environment.ExtensibleEnvironmentBaseTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestMixedEnvironment : ExtensibleEnvironmentBaseTest({
    MixedImmutableEnvironment.build(it)
}) {
    @Test
    fun `Base tests`() {
        runTests()
    }

    @Test
    fun `Test object injection is lazy`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { ElementClass() },
                entryOf { OtherElementClass() }
            ),
            EnvironmentContext(mapOf())
        )
        val env = MixedImmutableEnvironment(context)
        var wasInjected = false
        val inj = env.createInjector(Identifier(ElementClass::class)) { wasInjected = true }
        assertFalse(wasInjected)
        inj.getValue(FakeComponent, FakeComponent::fakeProperty)
        assertTrue(wasInjected)
    }

    class AtoB(scope: InjectionScope) {
        private val b: BtoA by scope()

        val className = "AtoB"

        fun useB() = b.className
    }

    class BtoA(scope: InjectionScope) {
        private val a: AtoB by scope()

        val className = "BtoA"

        fun useA() = a.className
    }

    class CtoC(scope: InjectionScope) {
        private val c: CtoC by scope()

        private val className = "CtoC"

        fun useC() = c.className
    }

    @Test
    fun `Test supports cyclic dependencies`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { AtoB(scope) },
                entryOf { BtoA(scope) }
            ),
            EnvironmentContext(mapOf())
        )
        val env = MixedImmutableEnvironment(context)
        val a = env.get<AtoB>()
        val b = env.get<BtoA>()
        assertEquals("BtoA", a.useB())
        assertEquals("AtoB", b.useA())
    }

    @Test
    fun `Test supports self injection`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { CtoC(scope) }
            ),
            EnvironmentContext(mapOf())
        )
        val env = MixedImmutableEnvironment(context)
        val c = env.get<CtoC>()
        assertEquals("CtoC", c.useC())
    }
}
