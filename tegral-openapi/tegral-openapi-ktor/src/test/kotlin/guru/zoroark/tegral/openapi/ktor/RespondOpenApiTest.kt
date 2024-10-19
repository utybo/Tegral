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

package guru.zoroark.tegral.openapi.ktor

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class RespondOpenApiTest {
    private fun ApplicationTestBuilder.setupTestApplication() {
        install(TegralOpenApiKtor) {
            title = "My Document"
            version = "0.0.0"
            description = "This is my document"
        }
        routing {
            openApiEndpoint("/openapi")
        }
    }

    private val expectedJsonOutput = """
        {"openapi":"3.0.1","info":{"title":"My Document","description":"This is my document","version":"0.0.0"}}
    """.trimIndent()
    private val expectedJsonOutput31 = """
        {"openapi":"3.1.0","info":{"title":"My Document","description":"This is my document","version":"0.0.0"},~
        "jsonSchemaDialect":"https://spec.openapis.org/oas/3.1/dialect/base"}
    """.trimIndent().replace("~\n", "")

    private val expectedYamlOutput = """
        openapi: 3.0.1
        info:
          title: My Document
          description: This is my document
          version: 0.0.0
    """.trimIndent() + "\n"
    private val expectedYamlOutput31 = """
        openapi: 3.1.0
        info:
          title: My Document
          description: This is my document
          version: 0.0.0
        jsonSchemaDialect: https://spec.openapis.org/oas/3.1/dialect/base
    """.trimIndent() + "\n"

    @Test
    fun `Respond OpenAPI defaults to JSON 3 0`() = testApplication {
        setupTestApplication()
        val response = client.get("/openapi")
        val result = response.bodyAsText()
        assertEquals(expectedJsonOutput, result)
        assertEquals(ContentType.Application.Json, response.contentType())
    }

    @Test
    fun `Respond OpenAPI with 3 0 version parameter`() = testApplication {
        setupTestApplication()
        val response = client.get("/openapi?version=3.0")
        val result = response.bodyAsText()
        assertEquals(expectedJsonOutput, result)
        assertEquals(ContentType.Application.Json, response.contentType())
    }

    @Test
    fun `Respond OpenAPI with 3 1 version parameter`() = testApplication {
        setupTestApplication()
        val response = client.get("/openapi?version=3.1")
        val result = response.bodyAsText()
        assertEquals(expectedJsonOutput31, result)
        assertEquals(ContentType.Application.Json, response.contentType())
    }

    @Test
    fun `Respond OpenAPI with invalid version parameter`() = testApplication {
        setupTestApplication()
        val response = client.get("/openapi?version=invalid")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Invalid version", response.bodyAsText())
    }

    @Test
    fun `Respond OpenAPI with json format parameter`() = testApplication {
        setupTestApplication()
        val response = client.get("/openapi?format=json")
        val result = response.bodyAsText()
        assertEquals(expectedJsonOutput, result)
        assertEquals(ContentType.Application.Json, response.contentType())
    }

    @Test
    fun `Respond OpenAPI with yaml format parameter`() = testApplication {
        setupTestApplication()
        val response = client.get("/openapi?format=yaml")
        val result = response.bodyAsText()
        assertEquals(expectedYamlOutput, result)
        assertEquals(ContentType("text", "yaml").withParameter("charset", "UTF-8"), response.contentType())
    }

    @Test
    fun `Respond OpenAPI with invalid format parameter`() = testApplication {
        setupTestApplication()
        val response = client.get("/openapi?format=invalid")
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Invalid format", response.bodyAsText())
    }

    @Test
    fun `Respond OpenAPI with all non default parameters`() = testApplication {
        setupTestApplication()
        val response = client.get("/openapi?version=3.1&format=yaml")
        val result = response.bodyAsText()
        assertEquals(expectedYamlOutput31, result)
        assertEquals(ContentType("text", "yaml").withParameter("charset", "UTF-8"), response.contentType())
    }
}
