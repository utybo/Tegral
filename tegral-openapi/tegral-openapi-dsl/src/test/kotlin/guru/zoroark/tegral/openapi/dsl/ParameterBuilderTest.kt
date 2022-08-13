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
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.CookieParameter
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter
import kotlin.test.Test
import kotlin.test.assertEquals

class ParameterBuilderTest {
    private val stringListSchema = ArraySchema().apply {
        items = StringSchema()
    }

    private val contextMock = mockk<OpenApiDslContext> {
        every { computeAndRegisterSchema(any()) } returns stringListSchema
    }

    @Test
    fun `Full query parameter`() {
        val param = ParameterBuilder(
            contextMock,
            "my_parameter",
            ParameterKind.Query
        ).apply {
            description = "My parameter's description"
            required()
            deprecated = false
            @Suppress("DEPRECATION")
            allowEmptyValue = true
            style = Parameter.StyleEnum.PIPEDELIMITED
            explode = true
            allowReserved = true

            schema<List<String>>(listOf("foo", "bar"))
        }.build()

        val expected = QueryParameter().apply {
            name = "my_parameter"
            description = "My parameter's description"
            required = true
            deprecated = false
            allowEmptyValue = true
            style = Parameter.StyleEnum.PIPEDELIMITED
            explode = true
            allowReserved = true
            schema = stringListSchema
            example = listOf("foo", "bar")
        }

        assertEquals(expected, param)
    }

    @Test
    fun `Full cookie parameter`() {
        val param = ParameterBuilder(
            contextMock,
            "my_parameter",
            ParameterKind.Cookie
        ).apply {
            description = "My parameter's description"
            required()
            deprecated = false
            @Suppress("DEPRECATION")
            allowEmptyValue = true
            style = Parameter.StyleEnum.PIPEDELIMITED
            explode = true
            allowReserved = true

            schema<List<String>>(listOf("foo", "bar"))
        }.build()

        val expected = CookieParameter().apply {
            name = "my_parameter"
            description = "My parameter's description"
            required = true
            deprecated = false
            allowEmptyValue = true
            style = Parameter.StyleEnum.PIPEDELIMITED
            explode = true
            allowReserved = true
            schema = stringListSchema
            example = listOf("foo", "bar")
        }

        assertEquals(expected, param)
    }

    @Test
    fun `Full header parameter`() {
        val param = ParameterBuilder(
            contextMock,
            "my_parameter",
            ParameterKind.Header
        ).apply {
            description = "My parameter's description"
            required()
            deprecated = false
            @Suppress("DEPRECATION")
            allowEmptyValue = true
            style = Parameter.StyleEnum.PIPEDELIMITED
            explode = true
            allowReserved = true

            schema<List<String>>(listOf("foo", "bar"))
        }.build()

        val expected = HeaderParameter().apply {
            name = "my_parameter"
            description = "My parameter's description"
            required = true
            deprecated = false
            allowEmptyValue = true
            style = Parameter.StyleEnum.PIPEDELIMITED
            explode = true
            allowReserved = true
            schema = stringListSchema
            example = listOf("foo", "bar")
        }

        assertEquals(expected, param)
    }

    @Test
    fun `Full path parameter`() {
        val param = ParameterBuilder(
            contextMock,
            "my_parameter",
            ParameterKind.Path
        ).apply {
            description = "My parameter's description"
            required()
            deprecated = false
            @Suppress("DEPRECATION")
            allowEmptyValue = true
            style = Parameter.StyleEnum.PIPEDELIMITED
            explode = true
            allowReserved = true

            schema<List<String>>(listOf("foo", "bar"))
        }.build()

        val expected = PathParameter().apply {
            name = "my_parameter"
            description = "My parameter's description"
            required = true
            deprecated = false
            allowEmptyValue = true
            style = Parameter.StyleEnum.PIPEDELIMITED
            explode = true
            allowReserved = true
            schema = stringListSchema
            example = listOf("foo", "bar")
        }

        assertEquals(expected, param)
    }

    @Test
    fun `Parameter with nothing else`() {
        val param = ParameterBuilder(
            contextMock,
            "my_parameter",
            ParameterKind.Query
        ).build()

        val expected = QueryParameter().apply {
            name = "my_parameter"
        }

        assertEquals(expected, param)
    }
}
