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

package guru.zoroark.tegral.web.apptest

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.web.controllers.KtorController
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.ktor.utils.io.charsets.Charset
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class IntegrationTestTest {
    class ExampleSimpleController : KtorController() {
        override fun Routing.install() {
            get("/") {
                call.respondText("OK!")
            }
        }
    }

    class SimpleIntegrationTest : TegralWebIntegrationTest({
        put(::ExampleSimpleController)
    }) {
        fun testOk() = test {
            val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals(ContentType.Text.Plain.withCharset(Charset.forName("UTF-8")), response.contentType())
            assertEquals("OK!", response.bodyAsText())
        }
    }

    @Test
    fun `Test simple integration test setup`() {
        assertDoesNotThrow { SimpleIntegrationTest().testOk() }
    }
}
