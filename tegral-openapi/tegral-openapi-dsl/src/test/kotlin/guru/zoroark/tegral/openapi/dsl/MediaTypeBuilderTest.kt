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
        val context = mockk<OpenApiDslContext> {
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
        // The MediaType.equals() function does not take the exampleSetFlag into account, so we have to check it
        // separately
        assertEquals(expected.exampleSetFlag, mediaType.exampleSetFlag)
    }

    @Test
    fun `Schema via reified inline`() {
        val context = mockk<OpenApiDslContext> {
            every { computeAndRegisterSchema(typeOf<String>()) } returns StringSchema()
        }
        val mediaType = MediaTypeBuilder(context).apply {
            schema<String>()
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
            exampleSetFlag = false // See https://github.com/utybo/Tegral/issues/40
        }

        assertEquals(expected, mediaType)
        // The MediaType.equals() function does not take the exampleSetFlag into account, so we have to check it
        // separately
        assertEquals(expected.exampleSetFlag, mediaType.exampleSetFlag)
    }

    @Test
    fun `Schema via reified inline with null example`() {
        val context = mockk<OpenApiDslContext> {
            every { computeAndRegisterSchema(typeOf<String?>()) } returns StringSchema()
        }
        val mediaType = MediaTypeBuilder(context).apply {
            schema<String?>(example = null)
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
            example = null
            exampleSetFlag = true // See https://github.com/utybo/Tegral/issues/40
        }

        assertEquals(expected, mediaType)
        // The MediaType.equals() function does not take the exampleSetFlag into account, so we have to check it
        // separately
        assertEquals(expected.exampleSetFlag, mediaType.exampleSetFlag)
    }

    @Test
    fun `Manually set example and schema`() {
        val context = mockk<OpenApiDslContext>()
        val mediaType = MediaTypeBuilder(context).apply {
            schema = StringSchema()
            example = "Test"
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
            example = "Test"
        }

        assertEquals(expected, mediaType)
        // The MediaType.equals() function does not take the exampleSetFlag into account, so we have to check it
        // separately
        assertEquals(expected.exampleSetFlag, mediaType.exampleSetFlag)
    }

    @Test
    fun `Schema and example via ktype`() {
        val context = mockk<OpenApiDslContext> {
            every { computeAndRegisterSchema(typeOf<String>()) } returns StringSchema()
        }
        val mediaType = MediaTypeBuilder(context).apply {
            schema(typeOf<String>(), "Test")
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
            example = "Test"
            exampleSetFlag = true
        }

        assertEquals(expected, mediaType)
        // The MediaType.equals() function does not take the exampleSetFlag into account, so we have to check it
        // separately
        assertEquals(expected.exampleSetFlag, mediaType.exampleSetFlag)
    }

    @Test
    fun `Schema via ktype`() {
        val context = mockk<OpenApiDslContext> {
            every { computeAndRegisterSchema(typeOf<String>()) } returns StringSchema()
        }
        val mediaType = MediaTypeBuilder(context).apply {
            schema(typeOf<String>())
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
            exampleSetFlag = false
        }

        assertEquals(expected, mediaType)
        // The MediaType.equals() function does not take the exampleSetFlag into account, so we have to check it
        // separately
        assertEquals(expected.exampleSetFlag, mediaType.exampleSetFlag)
    }

    @Test
    fun `Schema via ktype with null example`() {
        val context = mockk<OpenApiDslContext> {
            every { computeAndRegisterSchema(typeOf<String>()) } returns StringSchema()
        }
        val mediaType = MediaTypeBuilder(context).apply {
            schema(typeOf<String>(), null)
        }.build()

        val expected = MediaType().apply {
            schema = StringSchema()
            example = null
            exampleSetFlag = true
        }

        assertEquals(expected, mediaType)
        // The MediaType.equals() function does not take the exampleSetFlag into account, so we have to check it
        // separately
        assertEquals(expected.exampleSetFlag, mediaType.exampleSetFlag)
    }
}
