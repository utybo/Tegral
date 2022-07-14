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

import guru.zoroark.tegral.di.test.mockk.putMock
import guru.zoroark.tegral.examples.simplejsonapi.Greeting
import guru.zoroark.tegral.examples.simplejsonapi.GreetingController
import guru.zoroark.tegral.examples.simplejsonapi.GreetingService
import guru.zoroark.tegral.web.controllers.test.TegralControllerTest
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.mockk.every
import kotlin.test.Test
import kotlin.test.assertEquals

class GreetingControllerTest : TegralControllerTest<GreetingController>(::GreetingController) {
    @Test
    fun `Call greet endpoint`() = test {
        putMock<GreetingService> {
            every { greet(null) } returns Greeting("A", "B")
        }
        val response = client.get("/greet")
        val result = response.body<Greeting>()
        assertEquals(Greeting("A", "B"), result)
    }

    @Test
    fun `Call greet foobar endpoint`() = test {
        putMock<GreetingService> {
            every { greet("foobar") } returns Greeting("A", "B")
        }
        val response = client.get("/greet/foobar")
        val result = response.body<Greeting>()
        assertEquals(Greeting("A", "B"), result)
    }
}
