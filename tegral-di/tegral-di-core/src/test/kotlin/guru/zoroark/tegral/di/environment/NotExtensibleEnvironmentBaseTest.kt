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

import guru.zoroark.tegral.di.NotExtensibleException
import guru.zoroark.tegral.di.entryOf
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

@Suppress("UnnecessaryAbstractClass")
abstract class NotExtensibleEnvironmentBaseTest(
    private val provider: (EnvironmentContext) -> InjectionEnvironment
) : EnvironmentBaseTest(provider) {
    class B
    class A(scope: InjectionScope) {
        val b: B by scope.meta()
    }

    @Test
    fun `(Not extensible) Attempting meta injection should fail`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf {
                    A(scope)
                }
            )
        )
        assertThrows<NotExtensibleException> {
            provider(context)
        }
    }
}
