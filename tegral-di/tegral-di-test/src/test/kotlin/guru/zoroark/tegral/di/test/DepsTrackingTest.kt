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

package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.InvalidDeclarationException
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.getOrNull
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.test.check.DependencyTrackingInjectionEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DepsTrackingTest {
    @Test
    fun `Cannot call getOrNull`() {
        val env = tegralDi(DependencyTrackingInjectionEnvironment) {
        }
        assertFailsWith<NotAvailableInTestEnvironmentException>(
            message = "Not available on this kind of environment"
        ) {
            env.getOrNull<String>()
        }
    }

    class Foo
    class UnsafeComponent(scope: InjectionScope) {
        private val foo: Foo by scope()

        init {
            println("Hello $foo")
        }
    }

    @Test
    fun `Fails with hint on unsafe injection`() {
        assertFailsWith<InvalidDeclarationException>(
            message = "An unsafe injection was performed while trying to resolve dependencies. Use the " +
                "'safeInjection' check for more information."
        ) {
            tegralDi(DependencyTrackingInjectionEnvironment) {
                put(::Foo)
                put(::UnsafeComponent)
            }
        }
    }

    class A
    class B

    @Suppress("unused")
    class C(scope: InjectionScope) {
        private val a: A by scope()
        private val b: B by scope()
    }

    @Test
    fun `getAllIdentifiers is correct`() {
        val env = tegralDi(DependencyTrackingInjectionEnvironment) {
            put(::A)
            put(::B)
            put(::C)
        }

        val expected = setOf(
            Identifier(A::class),
            Identifier(B::class),
            Identifier(C::class)
        )
        assertEquals(expected, env.getAllIdentifiers().toSet())
    }
}
