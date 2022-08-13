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
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.RequestBody
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

class RequestBodyBuilderTest {
    data class TestSchema(val foo: String, val bar: String)

    @Test
    fun `Test with content`() {
        val mockSchema = mockk<Schema<*>>()
        val mockExample = mockk<TestSchema>()
        val context = mockk<OpenApiDslContext> {
            every { computeAndRegisterSchema(typeOf<TestSchema>()) } returns mockSchema
        }
        val response = RequestBodyBuilder(context).apply {
            description = "My request body description"
            required = true
            json { schema<TestSchema>(mockExample) }
        }.build()

        val expected = RequestBody().apply {
            description = "My request body description"
            required = true
            content = Content().apply {
                addMediaType(
                    "application/json",
                    MediaType().apply {
                        // /!\ This is order sensitive. If the schema is set before the example, it will attempt to call
                        // an unmocked 'cast' method on the schema. (can't be mocked either because it's set as
                        // protected)
                        example = mockExample
                        schema = mockSchema
                    }
                )
            }
        }

        assertEquals(expected, response)
    }

    @Test
    fun `Test without content`() {
        val context = mockk<OpenApiDslContext>()
        val response = RequestBodyBuilder(context).apply {
            required = false
            description = "My response description"
        }.build()

        val expected = RequestBody().apply {
            required = false
            description = "My response description"
        }

        assertEquals(expected, response)
    }
}
