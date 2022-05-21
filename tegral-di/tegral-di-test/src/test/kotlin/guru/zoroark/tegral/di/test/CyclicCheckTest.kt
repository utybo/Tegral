package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
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

                +noCycle
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

                +noCycle
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

                +noCycle
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
}
