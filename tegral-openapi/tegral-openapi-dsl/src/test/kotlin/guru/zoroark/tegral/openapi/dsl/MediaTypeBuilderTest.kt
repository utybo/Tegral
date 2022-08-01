package guru.zoroark.tegral.openapi.dsl

import io.mockk.every
import io.mockk.mockk
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.StringSchema
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

class MediaTypeBuilderTest {
    @Test
    fun `Schema and example via reified inline`() {
        val context = mockk<KoaDslContext> {
            every { computeAndRegisterSchema(typeOf<String>()) } returns StringSchema()
        }
        val mediaType = MediaTypeBuilder(context).apply {
            schema("Test")
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
            example = "Test"
        }

        assertEquals(expected, mediaType)
    }

    @Test
    fun `Schema via reified inline`() {
        val context = mockk<KoaDslContext> {
            every { computeAndRegisterSchema(typeOf<String>()) } returns StringSchema()
        }
        val mediaType = MediaTypeBuilder(context).apply {
            schema<String>()
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
        }

        assertEquals(expected, mediaType)
    }

    @Test
    fun `Manually set example and schema`() {
        val context = mockk<KoaDslContext>()
        val mediaType = MediaTypeBuilder(context).apply {
            schema = StringSchema()
            example = "Test"
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
            example = "Test"
        }

        assertEquals(expected, mediaType)
    }

    @Test
    fun `Schema and example via ktype`() {
        val context = mockk<KoaDslContext> {
            every { computeAndRegisterSchema(typeOf<String>()) } returns StringSchema()
        }
        val mediaType = MediaTypeBuilder(context).apply {
            schema(typeOf<String>(), "Test")
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
            example = "Test"
        }

        assertEquals(expected, mediaType)
    }

    @Test
    fun `Schema via ktype`() {
        val context = mockk<KoaDslContext> {
            every { computeAndRegisterSchema(typeOf<String>()) } returns StringSchema()
        }
        val mediaType = MediaTypeBuilder(context).apply {
            schema(typeOf<String>())
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
        }

        assertEquals(expected, mediaType)
    }
}
