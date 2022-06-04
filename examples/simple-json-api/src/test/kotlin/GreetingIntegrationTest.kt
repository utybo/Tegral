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

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.examples.simplejsonapi.Greeting
import guru.zoroark.tegral.examples.simplejsonapi.appModule
import guru.zoroark.tegral.web.apptest.TegralWebIntegrationTest
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlin.test.Test
import kotlin.test.assertEquals

class GreetingIntegrationTest : TegralWebIntegrationTest({
    put(appModule)
}) {
    @Test
    fun `Full test without argument`() = test {
        val result = client.get("/greet").body<Greeting>()
        assertEquals(Greeting("hello", "world"), result)
    }

    @Test
    fun `Full test with argument`() = test {
        val result = client.get("/greet/you").body<Greeting>()
        assertEquals(Greeting("hello", "you"), result)
    }
}
