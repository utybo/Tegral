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
import guru.zoroark.tegral.di.environment.EmptyQualifier
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.Qualifier
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.environment.named
import guru.zoroark.tegral.di.test.check.NoUnusedCheckDsl
import guru.zoroark.tegral.di.test.check.TegralDiCheckException
import guru.zoroark.tegral.di.test.check.exclude
import guru.zoroark.tegral.di.test.check.modules
import guru.zoroark.tegral.di.test.check.noUnused
import guru.zoroark.tegral.di.test.check.tegralDiCheck
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class NoUnusedCheckTest {
    // Actually a cycle
    class A(scope: InjectionScope) {
        val b: B by scope()
    }

    class B(scope: InjectionScope) {
        val c: C by scope()
    }

    class C(scope: InjectionScope) {
        val a: A by scope()
    }

    class D(scope: InjectionScope) {
        val a: A by scope()
    }

    class E(scope: InjectionScope) {
        val a: A by scope()
    }

    @Test
    fun `Test regular situation`() {
        val module = tegralDiModule {
            put(NoUnusedCheckTest::A)
            put(NoUnusedCheckTest::B)
            put(NoUnusedCheckTest::C)
        }
        assertDoesNotThrow {
            tegralDiCheck {
                modules(module)

                noUnused()
            }
        }
    }

    @Test
    fun `Test single unused situation`() {
        val module = tegralDiModule {
            put(NoUnusedCheckTest::A)
            put(NoUnusedCheckTest::B)
            put(NoUnusedCheckTest::C)
            put(NoUnusedCheckTest::D)
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)

                noUnused()
            }
        }.assertMessage(
            """
            'noUnused' check failed.
            The following component is not injected anywhere, making it unused.
            --> guru.zoroark.tegral.di.test.NoUnusedCheckTest.D (<no qualifier>)
            
            If some or all of the components mentioned above are still used outside of injections (e.g. via a 'get' ^
            call on the environment), you can exclude them from this rule by adding them after the 'noUnused':
            
                +noUnused {
                    exclude<ExcludeThis>()
                    exclude<ExcludeThat>(named("exclude.that"))
                    exclude(ExcludeIt::class)
                    exclude(ExcludeMe::class, named("excluded"))
                }
            """.trimIndent().replace("^\n", "")
        )
    }

    @Test
    fun `Test many unused situation`() {
        val module = tegralDiModule {
            put(NoUnusedCheckTest::A)
            put(NoUnusedCheckTest::B)
            put(NoUnusedCheckTest::C)
            put(NoUnusedCheckTest::D)
            put(NoUnusedCheckTest::E)
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)

                noUnused()
            }
        }.assertMessage(
            """
            'noUnused' check failed.
            The following components are not injected anywhere, making them unused.
            --> guru.zoroark.tegral.di.test.NoUnusedCheckTest.D (<no qualifier>)
            --> guru.zoroark.tegral.di.test.NoUnusedCheckTest.E (<no qualifier>)
            
            If some or all of the components mentioned above are still used outside of injections (e.g. via a 'get' ^
            call on the environment), you can exclude them from this rule by adding them after the 'noUnused':
            
                +noUnused {
                    exclude<ExcludeThis>()
                    exclude<ExcludeThat>(named("exclude.that"))
                    exclude(ExcludeIt::class)
                    exclude(ExcludeMe::class, named("excluded"))
                }
            """.trimIndent().replace("^\n", "")
        )
    }

    private inline fun checkSuccessWithExclusion(
        dQualifier: Qualifier = EmptyQualifier,
        eQualifier: Qualifier = EmptyQualifier,
        crossinline dsl: NoUnusedCheckDsl.() -> Unit
    ) {
        val module = tegralDiModule {
            put(NoUnusedCheckTest::A)
            put(NoUnusedCheckTest::B)
            put(NoUnusedCheckTest::C)
            put(dQualifier, NoUnusedCheckTest::D)
            put(eQualifier, NoUnusedCheckTest::E)
        }
        assertDoesNotThrow {
            tegralDiCheck {
                modules(module)

                noUnused {
                    dsl()
                }
            }
        }
    }

    @Test
    fun `Ok with exclusion via reified, no qualifier`() {
        checkSuccessWithExclusion {
            exclude<D>()
            exclude<E>()
        }
    }

    @Test
    fun `Ok with exclusion via reified, with qualifier`() {
        checkSuccessWithExclusion(named("hello!"), named("bonjour !")) {
            exclude<D>(named("hello!"))
            exclude<E>(named("bonjour !"))
        }
    }

    @Test
    fun `Ok with exclusion via kclass, no qualifier`() {
        checkSuccessWithExclusion {
            exclude(D::class)
            exclude(E::class)
        }
    }

    @Test
    fun `Ok with exclusion via kclass, with qualifier`() {
        checkSuccessWithExclusion(named("goodbye!"), named("au revoir !")) {
            exclude(D::class, named("goodbye!"))
            exclude(E::class, named("au revoir !"))
        }
    }

    private inline fun checkFailureWithExclusion(
        dQualifier: Qualifier = EmptyQualifier,
        crossinline dsl: NoUnusedCheckDsl.() -> Unit
    ) {
        val module = tegralDiModule {
            put(NoUnusedCheckTest::A)
            put(NoUnusedCheckTest::B)
            put(NoUnusedCheckTest::C)
            put(dQualifier, NoUnusedCheckTest::D)
            put(NoUnusedCheckTest::E)
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)

                noUnused {
                    dsl()
                }
            }
        }.assertMessage(
            """
            'noUnused' check failed.
            The following component is not injected anywhere, making it unused.
            --> guru.zoroark.tegral.di.test.NoUnusedCheckTest.E (<no qualifier>)
            
            If some or all of the components mentioned above are still used outside of injections (e.g. via a 'get' ^
            call on the environment), you can exclude them from this rule by adding them after the 'noUnused':
            
                +noUnused {
                    exclude<ExcludeThis>()
                    exclude<ExcludeThat>(named("exclude.that"))
                    exclude(ExcludeIt::class)
                    exclude(ExcludeMe::class, named("excluded"))
                }
            """.trimIndent().replace("^\n", "")
        )
    }

    @Test
    fun `Ok with exclusion via kclass, yet error somewhere else, reified, no qualifier`() {
        checkFailureWithExclusion {
            exclude<D>()
        }
    }

    @Test
    fun `Ok with exclusion via kclass, yet error somewhere else, kclass, no qualifier`() {
        checkFailureWithExclusion {
            exclude(D::class)
        }
    }

    @Test
    fun `Ok with exclusion via kclass, yet error somewhere else, reified, name qualifier`() {
        checkFailureWithExclusion(named("buongiorno!")) {
            exclude<D>(named("buongiorno!"))
        }
    }

    @Test
    fun `Ok with exclusion via kclass, yet error somewhere else, kclass, name qualifier`() {
        checkFailureWithExclusion(named("buongiorno!")) {
            exclude(D::class, named("buongiorno!"))
        }
    }
}
