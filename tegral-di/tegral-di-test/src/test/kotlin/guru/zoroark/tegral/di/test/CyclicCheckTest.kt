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

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.environment.named
import guru.zoroark.tegral.di.extensions.putAlias
import guru.zoroark.tegral.di.test.check.TegralDiCheckException
import guru.zoroark.tegral.di.test.check.modules
import guru.zoroark.tegral.di.test.check.noCycle
import guru.zoroark.tegral.di.test.check.tegralDiCheck
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class CyclicCheckTest {
    // Non-cyclic
    class Foo

    @Suppress("UnusedPrivateMember", "unused")
    class Bar(scope: InjectionScope) {
        private val foo: Foo by scope()
    }

    // Cyclic A -> B -> A
    @Suppress("UnusedPrivateMember", "unused")
    class A(scope: InjectionScope) {
        private val b: B by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class B(scope: InjectionScope) {
        private val a: A by scope()
    }

    // Cyclic C -> D -> E -> F -> C
    //                    ------>
    @Suppress("UnusedPrivateMember", "unused")
    class C(scope: InjectionScope) {
        private val d: D by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class D(scope: InjectionScope) {
        private val e: E by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class E(scope: InjectionScope) {
        private val f: F by scope()
        private val c: C by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class F(scope: InjectionScope) {
        private val c: C by scope()
    }

    @Test
    fun `Test correct modules`() {
        val module = tegralDiModule {
            put(CyclicCheckTest::Foo)
            put(CyclicCheckTest::Bar)
        }
        assertDoesNotThrow {
            tegralDiCheck {
                modules(module)

                noCycle()
            }
        }
    }

    @Test
    fun `Test direct cycle`() {
        val module = tegralDiModule {
            put(CyclicCheckTest::A)
            put(CyclicCheckTest::B)
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)

                noCycle()
            }
        }.assertMessage(
            """
            'noCycle' check failed.
            Cyclic dependency found:
                guru.zoroark.tegral.di.test.CyclicCheckTest.A (<no qualifier>)
            --> guru.zoroark.tegral.di.test.CyclicCheckTest.B (<no qualifier>)
            --> guru.zoroark.tegral.di.test.CyclicCheckTest.A (<no qualifier>)

            Note: --> represents an injection (i.e. A --> B means 'A depends on B').
            """.trimIndent()
        )
    }

    @Test
    fun `Test 4-way cycle`() {
        val module = tegralDiModule {
            put(CyclicCheckTest::C)
            put(CyclicCheckTest::D)
            put(CyclicCheckTest::E)
            put(CyclicCheckTest::F)
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)

                noCycle()
            }
        }.assertMessage(
            """
            'noCycle' check failed.
            Cyclic dependency found:
                guru.zoroark.tegral.di.test.CyclicCheckTest.C (<no qualifier>)
            --> guru.zoroark.tegral.di.test.CyclicCheckTest.D (<no qualifier>)
            --> guru.zoroark.tegral.di.test.CyclicCheckTest.E (<no qualifier>)
            --> guru.zoroark.tegral.di.test.CyclicCheckTest.F (<no qualifier>)
            --> guru.zoroark.tegral.di.test.CyclicCheckTest.C (<no qualifier>)

            Note: --> represents an injection (i.e. A --> B means 'A depends on B').
            """.trimIndent()
        )
    }

    interface AliasAContract
    class AliasAImpl(scope: InjectionScope) : AliasAContract {
        val b: AliasBContract by scope()
    }

    interface AliasBContract
    class AliasBImpl(scope: InjectionScope) : AliasBContract {
        val a: AliasAContract by scope()
    }

    @Test
    fun `Test single-element cycle via alias`() {
        val module = tegralDiModule {
            putAlias<AliasAContract, AliasAContract>()
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)

                noCycle()
            }
        }.assertMessage(
            """
            'noCycle' check failed.
            Cyclic dependency found:
                guru.zoroark.tegral.di.test.CyclicCheckTest.AliasAContract (<no qualifier>)
            R-> guru.zoroark.tegral.di.test.CyclicCheckTest.AliasAContract (<no qualifier>)

            Note: --> represents an injection (i.e. A --> B means 'A depends on B').
                  R-> represents a resolution dependency (e.g. an alias being resolved).
            """.trimIndent()
        )
    }

    @Test
    fun `Test two-elements cycle only via alias`() {
        val module = tegralDiModule {
            putAlias<AliasAContract, AliasAContract>(targetQualifier = named("second"))
            putAlias<AliasAContract, AliasAContract>(aliasQualifier = named("second"))
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)

                noCycle()
            }
        }.assertMessage(
            """
            'noCycle' check failed.
            Cyclic dependency found:
                guru.zoroark.tegral.di.test.CyclicCheckTest.AliasAContract (<no qualifier>)
            R-> guru.zoroark.tegral.di.test.CyclicCheckTest.AliasAContract (NameQualifier(name=second))
            R-> guru.zoroark.tegral.di.test.CyclicCheckTest.AliasAContract (<no qualifier>)

            Note: --> represents an injection (i.e. A --> B means 'A depends on B').
                  R-> represents a resolution dependency (e.g. an alias being resolved).
            """.trimIndent()
        )
    }

    @Test
    fun `Test multi-element cycle with alias and regular injection`() {
        val module = tegralDiModule {
            put(::AliasAImpl)
            putAlias<AliasAContract, AliasAImpl>()

            put(::AliasBImpl)
            putAlias<AliasBContract, AliasBImpl>()
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)

                noCycle()
            }
        }.assertMessage(
            """
            'noCycle' check failed.
            Cyclic dependency found:
                guru.zoroark.tegral.di.test.CyclicCheckTest.AliasAImpl (<no qualifier>)
            --> guru.zoroark.tegral.di.test.CyclicCheckTest.AliasBContract (<no qualifier>)
            R-> guru.zoroark.tegral.di.test.CyclicCheckTest.AliasBImpl (<no qualifier>)
            --> guru.zoroark.tegral.di.test.CyclicCheckTest.AliasAContract (<no qualifier>)
            R-> guru.zoroark.tegral.di.test.CyclicCheckTest.AliasAImpl (<no qualifier>)

            Note: --> represents an injection (i.e. A --> B means 'A depends on B').
                  R-> represents a resolution dependency (e.g. an alias being resolved).
            """.trimIndent()
        )
    }
}
