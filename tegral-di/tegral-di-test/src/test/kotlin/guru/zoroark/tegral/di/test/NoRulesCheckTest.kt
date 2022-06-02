package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.test.check.TegralDiCheckException
import guru.zoroark.tegral.di.test.check.modules
import guru.zoroark.tegral.di.test.check.tegralDiCheck
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

class NoRulesCheckTest {
    @Test
    fun `Tegral DI check without rules throws`() {
        val module = tegralDiModule {
            put(::NoRulesCheckTest)
        }
        assertThrows<TegralDiCheckException> {
            tegralDiCheck {
                modules(module)
            }
        }.assertMessage(
            // TODO update documentation link
            """
            tegralDiCheck called without any rule, which checks nothing.
            --> Add rules using +ruleName (for example '+complete', do not forget the +)
            --> If you do not want to run any checks, remove the tegralDiCheck block entirely.
            For more information, visit https://shedinja.zoroark.guru/ShedinjaCheck
            """.trimIndent()
        )
    }
}

fun TegralDiCheckException.assertMessage(expected: String) {
    assertEquals(expected, message)
}
