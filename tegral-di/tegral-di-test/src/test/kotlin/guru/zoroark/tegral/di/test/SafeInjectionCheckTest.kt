package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.test.check.TegralDiCheckException
import guru.zoroark.tegral.di.test.check.modules
import guru.zoroark.tegral.di.test.check.safeInjection
import guru.zoroark.tegral.di.test.check.tegralDiCheck
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class SafeInjectionCheckTest {
    @Suppress("UnusedPrivateMember", "unused")
    class A(scope: InjectionScope) {
        private val b: B by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class B(scope: InjectionScope) {
        private val a: A by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class C(scope: InjectionScope) {
        private val a: A by scope()
        private val b: B by scope()
    }

    @Suppress("UnusedPrivateMember", "unused")
    class D(scope: InjectionScope) {
        private val c: C by scope()
        private val cThing: C = c
    }

    @Test
    fun `Does not trigger on regular situation`() {
        val module = tegralDiModule {
            put(SafeInjectionCheckTest::A)
            put(SafeInjectionCheckTest::B)
            put(SafeInjectionCheckTest::C)
        }
        assertDoesNotThrow {
            tegralDiCheck {
                modules(module)

                +safeInjection
            }
        }
    }

    @Test
    @Suppress("MaxLineLength")
    fun `Triggers on dangerous injection`() {
        val module = tegralDiModule {
            put(SafeInjectionCheckTest::A)
            put(SafeInjectionCheckTest::B)
            put(SafeInjectionCheckTest::C)
            put(SafeInjectionCheckTest::D)
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)

                +safeInjection
            }
        }.assertMessage(
            """
            'safeInjection' check failed.
            The following injection is done during the instantiation of guru.zoroark.tegral.di.test.SafeInjectionCheckTest.D (<no qualifier>):
                guru.zoroark.tegral.di.test.SafeInjectionCheckTest.D (<no qualifier>)
            --> guru.zoroark.tegral.di.test.SafeInjectionCheckTest.C (<no qualifier>)

            You *must not* actually perform injections during the instantiation of objects.
            If you need to do something on an object provided by an environment before storing it as a property, use 'wrapIn' instead. See the documentation on the 'safeInjection' check for more details.
            """.trimIndent()
        )
    }
}
