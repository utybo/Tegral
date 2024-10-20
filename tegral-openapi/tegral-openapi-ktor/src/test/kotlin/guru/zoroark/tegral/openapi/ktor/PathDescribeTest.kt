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

import guru.zoroark.tegral.openapi.dsl.schema
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingHandler
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.options
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.testing.testApplication
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.IntegerSchema
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PathDescribeTest {
    private fun generateOpenApiForHelloWorld(method: HttpMethod): OpenAPI {
        return OpenAPI().apply {
            paths = Paths().addPathItem(
                "/foo",
                PathItem().apply {
                    operation(
                        method,
                        Operation().apply {
                            summary = "Returns hello world"
                            responses = ApiResponses().addApiResponse(
                                "200",
                                ApiResponse().apply {
                                    description = "Successfully greets the world"
                                    content = Content().addMediaType(
                                        "text/plain",
                                        MediaType().apply {
                                            schema = StringSchema()
                                            example = "Hello, world!"
                                        }
                                    )
                                }
                            )
                        }
                    )
                }
            )
        }
    }

    private fun testSimpleEndpoint(
        routeCreator: Route.(String, RoutingHandler) -> Route,
        method: HttpMethod
    ) = testApplication {
        install(TegralOpenApiKtor)
        routing {
            routeCreator("/foo") {
                call.respond("Hello, world!")
            } describe {
                summary = "Returns hello world"
                200 response {
                    description = "Successfully greets the world"
                    plainText { schema("Hello, world!") }
                }
            }
        }

        application {
            val result = openApi.buildOpenApiDocument()
            val expected = generateOpenApiForHelloWorld(method)

            assertEquals(expected, result)
        }
    }

    @Test
    fun `Test simple get endpoint`() {
        testSimpleEndpoint(Route::get, HttpMethod.GET)
    }

    @Test
    fun `Test simple post endpoint`() = testApplication {
        testSimpleEndpoint(Route::post, HttpMethod.POST)
    }

    @Test
    fun `Test simple put endpoint`() = testApplication {
        testSimpleEndpoint(Route::put, HttpMethod.PUT)
    }

    @Test
    fun `Test simple delete endpoint`() = testApplication {
        testSimpleEndpoint(Route::delete, HttpMethod.DELETE)
    }

    @Test
    fun `Test simple patch endpoint`() = testApplication {
        testSimpleEndpoint(Route::patch, HttpMethod.PATCH)
    }

    @Test
    fun `Test simple head endpoint`() = testApplication {
        testSimpleEndpoint(Route::head, HttpMethod.HEAD)
    }

    @Test
    fun `Test simple options endpoint`() = testApplication {
        testSimpleEndpoint(Route::options, HttpMethod.OPTIONS)
    }

    @Test
    fun `Test unknown method is ignored`() = testApplication {
        install(TegralOpenApiKtor)
        routing {
            route("/foo", io.ktor.http.HttpMethod("BLABLA")) {
                handle { call.respond("Hello, world!") }
            } describe {
                summary = "Returns hello world"
                200 response {
                    description = "Successfully greets the world"
                    plainText { schema("Hello, world!") }
                }
            }
        }

        application {
            val result = openApi.buildOpenApiDocument()
            val expected = OpenAPI()
            assertEquals(expected, result)
        }
    }

    @Test
    fun `Describe a path with path parameter`() = testApplication {
        install(TegralOpenApiKtor)

        routing {
            get("/foo/{id}") {
                call.respond("Foo!")
            } describe {
                summary = "Returns foo"
                "id" pathParameter {
                    description = "The id of the foo"
                    schema(123)
                }
                200 response {
                    description = "Foo was found"
                    plainText { schema("Foo!") }
                }
            }
        }

        application {
            val document = openApi.buildOpenApiDocument()
            val expected = OpenAPI().apply {
                paths = Paths().addPathItem(
                    "/foo/{id}",
                    PathItem().apply {
                        operation(
                            HttpMethod.GET,
                            Operation().apply {
                                summary = "Returns foo"
                                responses = ApiResponses().addApiResponse(
                                    "200",
                                    ApiResponse().apply {
                                        description = "Foo was found"
                                        content = Content().addMediaType(
                                            "text/plain",
                                            MediaType().apply {
                                                schema = StringSchema()
                                                example = "Foo!"
                                            }
                                        )
                                    }
                                )
                                parameters = listOf(
                                    PathParameter().apply {
                                        name = "id"
                                        description = "The id of the foo"
                                        schema = IntegerSchema()
                                        example = 123
                                    }
                                )
                            }
                        )
                    }
                )
            }

            assertEquals(expected, document)
        }
    }

    @Test
    fun `Does not fail if plugin is not installed`() = testApplication {
        assertDoesNotThrow {
            routing {
                get("/foo/bar") {
                } describe {
                    summary = "Hello"
                }
            }
        }
    }

    @Test
    fun `Test manual operation registration`() {
        val methods = mapOf<String, PathItem.() -> Operation>(
            "GET" to { get },
            "POST" to { post },
            "PUT" to { put },
            "PATCH" to { patch },
            "DELETE" to { delete },
            "HEAD" to { head },
            "OPTIONS" to { options }
        )
        for ((methodName, methodGetter) in methods) {
            val plugin = TegralOpenApiKtor()
            plugin.registerOperation("/test", io.ktor.http.HttpMethod(methodName)) {
                description = "Hello"
            }
            assertEquals("Hello", plugin.buildOpenApiDocument().paths["/test"]?.methodGetter()?.description)
        }
    }

    @Test
    fun `Unknown method is ignored`() {
        val plugin = TegralOpenApiKtor()
        plugin.registerOperation("/test", io.ktor.http.HttpMethod("FOOBAR")) {
            description = "Hello"
        }
        assertNull(plugin.buildOpenApiDocument().paths)
    }
}
