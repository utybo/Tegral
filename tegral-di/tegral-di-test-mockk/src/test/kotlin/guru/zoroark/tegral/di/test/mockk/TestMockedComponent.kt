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

package guru.zoroark.tegral.di.test.mockk

import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.test.TegralSubjectTest
import io.mockk.every
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

interface ExampleService {
    fun doSomething(): String
}

class ExampleServiceUser(scope: InjectionScope) {
    private val service: ExampleService by scope()

    fun useService(): String = service.doSomething()
}

class TestMockedComponent : TegralSubjectTest<ExampleServiceUser>(::ExampleServiceUser) {
    @Test
    fun `Test with mocked example service`() = test {
        val exampleService = putMock<ExampleService> {
            every { doSomething() } returns "OK"
        }
        assertEquals("OK", subject.useService())
        verify(exactly = 1) { exampleService.doSomething() }
    }
}
