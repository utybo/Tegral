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

package guru.zoroark.tegral.openapi.dsl.repro

import guru.zoroark.tegral.openapi.dsl.HeaderBuilder
import guru.zoroark.tegral.openapi.dsl.OpenApiDslContext
import io.mockk.every
import io.mockk.mockk
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
