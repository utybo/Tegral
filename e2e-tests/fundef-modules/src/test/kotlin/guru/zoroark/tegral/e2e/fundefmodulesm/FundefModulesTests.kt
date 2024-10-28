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

package guru.zoroark.tegral.e2e.fundefmodulesm

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.test.TegralSubjectTest
import guru.zoroark.tegral.di.test.mockk.putMock
import guru.zoroark.tegral.e2e.fundefmodules.Repository
import guru.zoroark.tegral.e2e.fundefmodules.Service
import guru.zoroark.tegral.e2e.fundefmodules.app
import guru.zoroark.tegral.e2e.fundefmodules.controller
import guru.zoroark.tegral.e2e.fundefmodules.openApi
import guru.zoroark.tegral.openapi.dsl.openApi
import guru.zoroark.tegral.openapi.dsl.toJson
import guru.zoroark.tegral.openapi.ktor.openApi
import guru.zoroark.tegral.web.appdefaults.DefaultKtorApplication
import guru.zoroark.tegral.web.controllers.test.TegralControllerTest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.Routing
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verifyAll
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull

class FundefRepositoryTest : TegralSubjectTest<Repository>(::Repository) {
    @Test
    fun `Persist then retrieve`() = test {
        subject.store("Hello")
        assertEquals("Hello", subject.retrieve())
    }

    @Test
    fun `Retrieve when not set`() = test {
        assertNull(subject.retrieve())
    }

    @Test
    fun `Store then delete`() = test {
        subject.store("Hello")
        subject.delete()
        assertNull(subject.retrieve())
    }
}

class FundefServiceTest : TegralSubjectTest<Service>(::Service) {
    @Test
    fun `Get passes through`() = test {
        val mockResult = "YEYEYEYEYE"
        putMock<Repository> {
            every { retrieve() } returns mockResult
        }

        assertEquals(mockResult, subject.getString())
    }

    @Test
    fun `Store with non-blank passes through`() = test {
        val mockRepository = putMock<Repository> {
            every { store("hello there") } returns "hello there"
        }

        assertEquals("hello there", subject.updateString("hello there"))

        verifyAll {
            mockRepository.store("hello there")
        }
    }

    @Test
    fun `Store with blank fails`() = test<Unit> {
        putMock<Repository> { }

        assertFails(message = "New string must not be blank") { subject.updateString("   \t") }
    }

    @Test
    fun `Reset string passes through`() = test {
        val mockRepository = putMock<Repository> {
            every { delete() } just runs
        }

        subject.resetString()

        verifyAll {
            mockRepository.delete()
        }
    }
}

class FundefControllerTest : TegralControllerTest<Any>(Routing::controller) {
    @Test
    fun `Get (not found)`() = test<Unit> {
        putMock<Service> {
            every { getString() } returns null
        }

        client.get("/string").apply {
            assertEquals(HttpStatusCode.NotFound, status)
            assertEquals("Not found", bodyAsText())
        }
    }

    @Test
    fun `Get (found)`() = test<Unit> {
        putMock<Service> {
            every { getString() } returns "Yeehaw"
        }

        client.get("/string").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Yeehaw", bodyAsText())
        }
    }

    @Test
    fun `Put (invalid)`() = test {
        val service = putMock<Service> {
            every { updateString("\n \t \t") } throws IllegalArgumentException("It's a no")
        }

        client.put("/string") {
            setBody("\n \t \t")
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, status)
            assertEquals("It's a no", bodyAsText())
        }

        verifyAll { service.updateString("\n \t \t") }
    }

    @Test
    fun `Put (valid)`() = test {
        val service = putMock<Service> {
            every { updateString("YEAH") } returns "YEAH"
        }

        client.put("/string") {
            setBody("YEAH")
        }.apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("YEAH", bodyAsText())
        }

        verifyAll { service.updateString("YEAH") }
    }

    @Test
    fun `Delete (ok)`() = test {
        val service = putMock<Service> {
            every { resetString() } just runs
        }

        client.delete("/string").apply {
            assertEquals(HttpStatusCode.OK, status)
        }

        verifyAll { service.resetString() }
    }
}

class FundefOpenapiTest {
    @Test
    fun `OpenAPI document`(): Unit = runBlocking {
        val tegralApp = app()
        try {
            val openApiDocument =
                tegralApp.environment.get<DefaultKtorApplication>().application.openApi.buildOpenApiDocument()
            val result = jacksonObjectMapper().readValue<Map<*, *>>(openApiDocument.toJson())
            val expected = jacksonObjectMapper().readValue<Map<*, *>>(
                """
                {
                  "openapi": "3.0.1",
                  "info": {
                    "title": "Fundef example",
                    "description": "An example of a Tegral application that uses fundefs to define modules, etc.",
                    "version": "0.0.0"
                  },
                  "paths": {
                    "/string": {
                      "get": {
                        "summary": "Get the current string value",
                        "responses": {
                          "200": {
                            "description": "Success",
                            "content": { "text/plain": { "schema": { "type": "string" } } }
                          },
                          "404": { "description": "No value currently available" }
                        }
                      },
                      "put": {
                        "summary": "Set the string value",
                        "requestBody": {
                          "content": { "text/plain": { "schema": { "type": "string" } } }
                        },
                        "responses": {
                          "200": { "description": "Set successfully" },
                          "400": { "description": "Invalid value" }
                        }
                      },
                      "delete": {
                        "summary": "Reset the string value",
                        "responses": { "200": { "description": "Reset successfully" } }
                      }
                    }
                  }
                }
                """.trimIndent()
            )
            assertEquals(expected, result)
        } finally {
            tegralApp.stop()
        }
    }
}
