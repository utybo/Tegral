package guru.zoroark.tegral.openapi.dsl.repro

import guru.zoroark.tegral.openapi.dsl.HeaderBuilder
import guru.zoroark.tegral.openapi.dsl.OpenApiDslContext
import io.mockk.every
import io.mockk.mockk
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.media.Schema
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HeaderBuilderTest {
    @Test
    fun `Header with all info`() {
        val schemaMock = mockk<Schema<*>>()
        val context = mockk<OpenApiDslContext> {
            every { computeAndRegisterSchema(any()) } returns schemaMock
        }
        val header = HeaderBuilder(context).apply {
            schema(mockk())
            description = "Hello!"
            deprecated = true
            explode = true
        }.build()
        assertEquals(schemaMock, header.schema)
        assertEquals("Hello!", header.description)
        assertTrue(header.deprecated)
        assertTrue(header.explode)
    }
}
