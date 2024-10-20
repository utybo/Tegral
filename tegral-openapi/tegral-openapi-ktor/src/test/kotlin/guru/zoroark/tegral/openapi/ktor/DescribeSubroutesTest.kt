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

import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.testing.testApplication
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class DescribeSubroutesTest {

    @Test
    @Suppress("LongMethod")
    fun `Describe subroutes adds hook on relevant subroutes`() = testApplication {
        install(TegralOpenApiKtor)
        routing {
            route("/foo") {
                get("/before") {} describe {
                    summary = "Before describeSubroutes"
                }

                describeSubroutes {
                    tags += "foo"
                }

                get("/bar") {} describe {
                    summary = "Bar endpoint"
                }

                get("/baz") {} describe {
                    summary = "Baz endpoint"
                }

                post {} describe {
                    summary = "Foo endpoint"
                }
            }

            get("/unrelated") {} describe {
                summary = "Unrelated endpoint"
            }
        }
        application {
            val document = openApi.buildOpenApiDocument()
            val expected = OpenAPI().apply {
                paths = Paths().apply {
                    addPathItem(
                        "/foo/before",
                        PathItem().apply {
                            get = Operation().apply {
                                summary = "Before describeSubroutes"
                            }
                        }
                    )

                    addPathItem(
                        "/foo/bar",
                        PathItem().apply {
                            get = Operation().apply {
                                summary = "Bar endpoint"
                                tags = listOf("foo")
                            }
                        }
                    )

                    addPathItem(
                        "/foo/baz",
                        PathItem().apply {
                            get = Operation().apply {
                                summary = "Baz endpoint"
                                tags = listOf("foo")
                            }
                        }
                    )

                    addPathItem(
                        "/foo",
                        PathItem().apply {
                            post = Operation().apply {
                                summary = "Foo endpoint"
                                tags = listOf("foo")
                            }
                        }
                    )

                    addPathItem(
                        "/unrelated",
                        PathItem().apply {
                            get = Operation().apply {
                                summary = "Unrelated endpoint"
                            }
                        }
                    )
                }
            }
            assertEquals(expected, document)
        }
    }

    @Test
    fun `Multiple subroute descriptions on same route`() = testApplication {
        install(TegralOpenApiKtor)
        routing {
            route("/foo") {
                describeSubroutes {
                    tags += "foo"
                }

                get("/yes") {} describe {
                    summary = "Yes endpoint"
                }

                describeSubroutes {
                    tags += "bar"
                }

                get("/bar") {} describe {
                    summary = "Bar endpoint"
                }
            }
        }
        application {
            val document = openApi.buildOpenApiDocument()
            val expected = OpenAPI().apply {
                paths = Paths().apply {
                    addPathItem(
                        "/foo/yes",
                        PathItem().apply {
                            get = Operation().apply {
                                summary = "Yes endpoint"
                                tags = listOf("foo")
                            }
                        }
                    )

                    addPathItem(
                        "/foo/bar",
                        PathItem().apply {
                            get = Operation().apply {
                                summary = "Bar endpoint"
                                tags = listOf("foo", "bar")
                            }
                        }
                    )
                }
            }
            assertEquals(expected, document)
        }
    }

    @Test
    fun `Nested subroute descriptions`() = testApplication {
        install(TegralOpenApiKtor)
        routing {
            route("/foo") {
                describeSubroutes {
                    tags += "foo"
                }

                route("/bar") {
                    describeSubroutes {
                        tags += "bar"
                    }

                    get("/baz") {} describe {
                        summary = "Baz endpoint"
                    }
                }
            }
        }
        application {
            val document = openApi.buildOpenApiDocument()
            val expected = OpenAPI().apply {
                paths = Paths().apply {
                    addPathItem(
                        "/foo/bar/baz",
                        PathItem().apply {
                            get = Operation().apply {
                                summary = "Baz endpoint"
                                tags = listOf("foo", "bar")
                            }
                        }
                    )
                }
            }
            assertEquals(expected, document)
        }
    }

    @Test
    fun `Describe subroutes without plugin installed does nothing`() = testApplication {
        routing {
            assertDoesNotThrow {
                describeSubroutes {}
            }
        }
    }
}
