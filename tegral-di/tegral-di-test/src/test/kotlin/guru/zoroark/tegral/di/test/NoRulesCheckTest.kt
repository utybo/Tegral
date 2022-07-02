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
            """
            tegralDiCheck called without any rule, which checks nothing.
            --> Add rules using +ruleName (for example '+complete', do not forget the +)
            --> If you do not want to run any checks, remove the tegralDiCheck block entirely.
            For more information, visit https://tegral.zoroark.guru/docs/core/di/testing/checks
            """.trimIndent()
        )
    }
}

fun TegralDiCheckException.assertMessage(expected: String) {
    assertEquals(expected, message)
}
