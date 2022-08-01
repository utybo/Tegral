package guru.zoroark.tegral.openapi.dsl

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.responses.ApiResponse
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

class ResponseBuilderTest {
    data class TestSchema(val foo: String, val bar: String)

    @Test
    fun `Test with content`() {
        val mockSchema = mockk<Schema<*>>()
        val mockExample = mockk<TestSchema>()
        val context = mockk<KoaDslContext> {
            every { computeAndRegisterSchema(typeOf<TestSchema>()) } returns mockSchema
        }
        val response = ResponseBuilder(context).apply {
            description = "My response description"
            json { schema<TestSchema>(mockExample) }
        }.build()

        val expected = ApiResponse().apply {
            description = "My response description"
            content = Content().apply {
                addMediaType("application/json", MediaType().apply {
                    // /!\ This is order sensitive. If the schema is set before the example, it will attempt to call an
                    // unmocked 'cast' method on the schema. (can't be mocked either because it's set as protected)
                    example = mockExample
                    schema = mockSchema
                })
            }
        }

        assertEquals(expected, response)
    }

    @Test
    fun `Test without content`() {
        val context = mockk<KoaDslContext>()
        val response = ResponseBuilder(context).apply {
            description = "My response description"
        }.build()

        val expected = ApiResponse().apply {
            description = "My response description"
        }

        assertEquals(expected, response)
    }
}
