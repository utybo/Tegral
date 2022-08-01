package guru.zoroark.tegral.openapi.dsl

import io.mockk.mockk
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
