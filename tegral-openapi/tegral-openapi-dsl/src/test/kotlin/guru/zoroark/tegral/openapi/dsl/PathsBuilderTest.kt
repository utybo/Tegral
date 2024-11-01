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

package guru.zoroark.tegral.openapi.dsl

import io.mockk.mockk
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PathsBuilderTest {
    @Test
    fun `Test adding regular operations`() {
        val paths = PathsBuilder(mockk()).apply {
            "/foo/bar" {
                get {
                    summary = "Gets foo bar"
                }
                post {
                    summary = "Creates foo bar"
                }
            }

            "/baz/qux" {
                get {
                    summary = "Gets baz qux"
                }

                put {
                    summary = "Edits baz qux"
                }
            }
        }.build()

        val expected = Paths().apply {
            addPathItem(
                "/foo/bar",
                PathItem().apply {
                    get = Operation().apply {
                        summary = "Gets foo bar"
                    }
                    post = Operation().apply {
                        summary = "Creates foo bar"
                    }
                }
            )
            addPathItem(
                "/baz/qux",
                PathItem().apply {
                    get = Operation().apply {
                        summary = "Gets baz qux"
                    }
                    put = Operation().apply {
                        summary = "Edits baz qux"
                    }
                }
            )
        }

        assertEquals(expected, paths)
    }

    @Test
    fun `Directly define a get path and operation`() {
        val paths = PathsBuilder(mockk()).apply {
            "/foo/bar" get {
                summary = "Gets foo bar"
            }
        }.build()

        val expected = Paths().apply {
            addPathItem(
                "/foo/bar",
                PathItem().apply {
                    get = Operation().apply {
                        summary = "Gets foo bar"
                    }
                }
            )
        }

        assertEquals(expected, paths)
    }

    @Test
    fun `Directly define a post path and operation`() {
        val paths = PathsBuilder(mockk()).apply {
            "/foo/bar" post {
                summary = "Creates foo bar"
            }
        }.build()

        val expected = Paths().apply {
            addPathItem(
                "/foo/bar",
                PathItem().apply {
                    post = Operation().apply {
                        summary = "Creates foo bar"
                    }
                }
            )
        }

        assertEquals(expected, paths)
    }

    @Test
    fun `Directly define a put path and operation`() {
        val paths = PathsBuilder(mockk()).apply {
            "/foo/bar" put {
                summary = "Edits foo bar"
            }
        }.build()

        val expected = Paths().apply {
            addPathItem(
                "/foo/bar",
                PathItem().apply {
                    put = Operation().apply {
                        summary = "Edits foo bar"
                    }
                }
            )
        }

        assertEquals(expected, paths)
    }

    @Test
    fun `Directly define a delete path and operation`() {
        val paths = PathsBuilder(mockk()).apply {
            "/foo/bar" delete {
                summary = "Deletes foo bar"
            }
        }.build()

        val expected = Paths().apply {
            addPathItem(
                "/foo/bar",
                PathItem().apply {
                    delete = Operation().apply {
                        summary = "Deletes foo bar"
                    }
                }
            )
        }

        assertEquals(expected, paths)
    }

    @Test
    fun `Directly define a patch path and operation`() {
        val paths = PathsBuilder(mockk()).apply {
            "/foo/bar" patch {
                summary = "Patches foo bar"
            }
        }.build()

        val expected = Paths().apply {
            addPathItem(
                "/foo/bar",
                PathItem().apply {
                    patch = Operation().apply {
                        summary = "Patches foo bar"
                    }
                }
            )
        }

        assertEquals(expected, paths)
    }

    @Test
    fun `Directly define a options path and operation`() {
        val paths = PathsBuilder(mockk()).apply {
            "/foo/bar" options {
                summary = "Options foo bar"
            }
        }.build()

        val expected = Paths().apply {
            addPathItem(
                "/foo/bar",
                PathItem().apply {
                    options = Operation().apply {
                        summary = "Options foo bar"
                    }
                }
            )
        }

        assertEquals(expected, paths)
    }

    @Test
    fun `Directly define a head path and operation`() {
        val paths = PathsBuilder(mockk()).apply {
            "/foo/bar" head {
                summary = "Heads foo bar"
            }
        }.build()

        val expected = Paths().apply {
            addPathItem(
                "/foo/bar",
                PathItem().apply {
                    head = Operation().apply {
                        summary = "Heads foo bar"
                    }
                }
            )
        }

        assertEquals(expected, paths)
    }

    @Suppress("LongMethod")
    @Test
    fun `Add everything via definition at path level`() {
        val paths = PathsBuilder(mockk()).apply {
            "/foo/bar" {
                summary = "Never"
                description = "gonna"
                externalDocsDescription = "give"
                externalDocsUrl = "you"
                operationId = "up"
                deprecated = true
                security("never")
                security("gonna", "let")
                security { requirement("you", "down") }
                200 response {
                    description = "gonna"
                }
                "let" pathParameter {
                    description = "you"
                }
                "down" headerParameter {
                    description = "never"
                }
                "gonna" cookieParameter {
                    description = "run"
                }
                "around" queryParameter {
                    description = "and"
                }
                get {
                    // ...
                }

                body {
                    description = "desert"
                }

                post {
                    // youuuuuu
                }
            }
        }.build()
        val path = assertNotNull(paths["/foo/bar"])
        val operations = listOf(
            assertNotNull(path.get),
            assertNotNull(path.post)
        )
        for (op in operations) {
            assertEquals(3, op.security.size)

            assertEquals("Never", op.summary)
            assertEquals("gonna", op.description)
            assertEquals("give", op.externalDocs.description)
            assertEquals("you", op.externalDocs.url)
            assertEquals("up", op.operationId)
            assertEquals(emptyList(), op.security[0]["never"])
            assertEquals(listOf("let"), op.security[1]["gonna"])
            assertEquals(listOf("down"), op.security[2]["you"])
            assertTrue(op.deprecated)
            assertEquals("gonna", op.responses["200"]?.description)
            assertEquals(4, op.parameters.size)
            val expectations = listOf("let" to "you", "down" to "never", "gonna" to "run", "around" to "and")
            for ((i, param) in op.parameters.withIndex()) {
                assertEquals(expectations[i].first, param.name)
                assertEquals(expectations[i].second, param.description)
            }
        }
        assertNull(path.get.requestBody)
        assertEquals("desert", path.post.requestBody.description)
    }

    @Test
    fun `Reading operation properties on paths fails`() {
        val ops = listOf<OperationDsl.() -> Unit>(
            { summary },
            { description },
            { externalDocsDescription },
            { externalDocsUrl },
            { requestBody },
            { deprecated },
            { operationId },
            { parameters },
            { securityRequirements },
            { responses }
        )
        for (op in ops) {
            val exc = assertFailsWith<IllegalStateException> {
                PathBuilder(mockk()).apply(op)
            }
            assertEquals(
                "Operation functions, when used on a path instead of an actual operation, are write-only",
                exc.message
            )
        }
    }
}
