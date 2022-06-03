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

package guru.zoroark.tegral.di.test.environment

import guru.zoroark.tegral.di.NotExtensibleException
import guru.zoroark.tegral.di.environment.EnvironmentContext
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.test.entryOf
import kotlin.test.assertFailsWith

/**
 * An [EnvironmentBaseTest] specifically for non-extensible environments.
 *
 * **Remember to have at least one test that executes [runTests], see [EnvironmentBaseTest]'s documentation for more
 * information.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class NotExtensibleEnvironmentBaseTest(
    private val provider: (EnvironmentContext) -> InjectionEnvironment
) : EnvironmentBaseTest(provider) {
    private class B
    private class A(scope: InjectionScope) {
        val b: B by scope.meta()
    }

    final override val additionalTests: List<Pair<String, () -> Unit>>
        get() = listOf(
            "(Not extensible) Attempting meta injection should fail" to
                this::`(Not extensible) Attempting meta injection should fail`
        )

    @Suppress("FunctionName")
    protected fun `(Not extensible) Attempting meta injection should fail`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf {
                    A(scope)
                }
            )
        )
        assertFailsWith<NotExtensibleException> {
            provider(context)
        }
    }
}
