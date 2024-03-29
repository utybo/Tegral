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

import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.extensions.EagerImmutableMetaEnvironment
import guru.zoroark.tegral.di.test.environment.ExtensibleEnvironmentBaseTest
import guru.zoroark.tegral.di.test.environment.NotExtensibleEnvironmentBaseTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class TestUnsafeMutableEnvironment : ExtensibleEnvironmentBaseTest(::UnsafeMutableEnvironment) {
    @Test
    fun `Base test`() {
        runTests()
    }

    @Test
    fun `Base test (on actual injection env)`() {
        val baseTest = object : NotExtensibleEnvironmentBaseTest({ context ->
            UnsafeMutableEnvironment.MutableEnvironment(null, context)
        }) {}
        baseTest.runTests()
    }

    @Test
    fun `Fails when using non-default meta environment`() {
        assertFailsWith<NotAvailableInTestEnvironmentException>(
            message = "Cannot create UnsafeMutableEnvironment with any meta environment other than " +
                "UnsafeMutableEnvironment.Meta"
        ) {
            tegralDi(UnsafeMutableEnvironment, EagerImmutableMetaEnvironment) { }
        }
    }

    @Test
    fun `Cannot directly create meta env`() {
        assertFailsWith<NotAvailableInTestEnvironmentException>(
            message = "UnsafeMutableEnvironment.Meta environments cannot be created directly."
        ) {
            @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // That's normal
            tegralDi(UnsafeMutableEnvironment.Meta) {
            }
        }
    }
}
