package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.test.check.TegralDiCheckException
import guru.zoroark.tegral.di.test.check.complete
import guru.zoroark.tegral.di.test.check.modules
import guru.zoroark.tegral.di.test.check.tegralDiCheck
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class CompleteCheckTest {
    @Suppress("UnusedPrivateMember", "unused")
    class A(scope: InjectionScope) {
        private val b: B by scope()
    }

    class B

    @Suppress("UnusedPrivateMember", "unused")
    class C(scope: InjectionScope) {
        private val z: Z by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class E(scope: InjectionScope) {
        private val b: B by scope()
        private val c: C by scope()
        private val z: Z by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class F(scope: InjectionScope) {
        private val a: A by scope()
        private val z: Z by scope()
        private val y: Y by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class G(scope: InjectionScope) {
        private val y: Y by scope()
    }

    class Y
    class Z

    @Suppress("UnusedPrivateMember", "unused")
    class MetaUserA(scope: InjectionScope) {
        private val y: Y by scope.meta()
    }

    @Test
    fun `Test OK case`() {
        val module = tegralDiModule {
            put(CompleteCheckTest::A)
            put(CompleteCheckTest::B)
        }

        assertDoesNotThrow {
            tegralDiCheck {
                modules(module)

                complete()
            }
        }
    }

    @Test
    fun `Test single missing case`() {
        val module = tegralDiModule {
            put(CompleteCheckTest::A)
            put(CompleteCheckTest::B)
            put(CompleteCheckTest::C)
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)
                complete()
            }
        }.assertMessage(
            """
            'complete' check failed.
            Some dependencies were not found. Make sure they are present within your module definitions.
            --> guru.zoroark.tegral.di.test.CompleteCheckTest.Z (<no qualifier>) not found
                Requested by:
                --> guru.zoroark.tegral.di.test.CompleteCheckTest.C (<no qualifier>)
            """.trimIndent()
        )
    }

    @Test
    fun `Test many missing case`() {
        val module = tegralDiModule {
            put(CompleteCheckTest::A)
            put(CompleteCheckTest::B)
            put(CompleteCheckTest::C)
            put(CompleteCheckTest::E)
        }
        val module2 = tegralDiModule {
            put(CompleteCheckTest::F)
            put(CompleteCheckTest::G)
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module, module2)
                complete()
            }
        }.assertMessage(
            """
            'complete' check failed.
            Some dependencies were not found. Make sure they are present within your module definitions.
            --> guru.zoroark.tegral.di.test.CompleteCheckTest.Z (<no qualifier>) not found
                Requested by:
                --> guru.zoroark.tegral.di.test.CompleteCheckTest.C (<no qualifier>)
                --> guru.zoroark.tegral.di.test.CompleteCheckTest.E (<no qualifier>)
                --> guru.zoroark.tegral.di.test.CompleteCheckTest.F (<no qualifier>)
            --> guru.zoroark.tegral.di.test.CompleteCheckTest.Y (<no qualifier>) not found
                Requested by:
                --> guru.zoroark.tegral.di.test.CompleteCheckTest.F (<no qualifier>)
                --> guru.zoroark.tegral.di.test.CompleteCheckTest.G (<no qualifier>)
            """.trimIndent()
        )
    }

    @Test
    fun `Test meta-environment dependencies are ignored`() {
        val module = tegralDiModule {
            put(CompleteCheckTest::A)
            put(CompleteCheckTest::B)
            put(CompleteCheckTest::MetaUserA)
        }
        assertDoesNotThrow {
            tegralDiCheck {
                modules(module)
                complete()
            }
        }
    }
}
