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

package guru.zoroark.tegral.openapi.ktorui

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TegralSwaggerUiKtorTest {
    private fun ApplicationTestBuilder.setup() {
        install(TegralSwaggerUiKtor) {}

        routing {
            swaggerUiEndpoint("/swagger", "/openapi/is/right/there")
        }
    }

    @Test
    fun `Redirect from base endpoint`() = testApplication {
        setup()
        val client = createClient {
            followRedirects = false
        }
        val response = client.get("/swagger")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/swagger/index.html", response.headers["Location"])
    }

    @Test
    fun `Returns expected index content`() = testApplication {
        setup()
        val response = client.get("/swagger/index.html")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("<body>"))
    }

    @Test
    fun `Returns expected index content with only trailing slash`() = testApplication {
        setup()
        val response = client.get("/swagger/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("<body>"))
    }

    @Test
    fun `Returns 404 when swagger resource not found`() = testApplication {
        setup()
        val response = client.get("/swagger/idonotexist")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `Returns swizzled initialize`() = testApplication {
        setup()

        val response = client.get("/swagger/swagger-initializer.js")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(response.bodyAsText().contains("url: \"/openapi/is/right/there\""), true)
    }
}
