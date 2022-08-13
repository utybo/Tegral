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
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import kotlin.test.Test
import kotlin.test.assertEquals

class RootBuilderTest {
    @Test
    fun `Test with everything`() {
        val context = mockk<OpenApiDslContext>()
        val openApi = RootBuilder(context).apply {
            // InfoDsl
            title = "Test document"
            version = "0.0.0"

            // TagsDsl
            "test" tag {}

            // PathsDsl
            "/test" get {}

            // RootDsl
            "scheme" securityScheme {}
            "http://localhost:8080" server {}
            externalDocsDescription = "External docs"
            externalDocsUrl = "https://example.com"
        }.build()

        val expected = OpenAPI().apply {
            info = Info().apply {
                title = "Test document"
                version = "0.0.0"
            }
            tags = listOf(
                Tag().apply { name = "test" }
            )
            path(
                "/test",
                PathItem().apply {
                    get = Operation()
                }
            )
            components = Components().apply {
                securitySchemes = mutableMapOf("scheme" to SecurityScheme())
            }
            servers = listOf(
                Server().apply {
                    url = "http://localhost:8080"
                }
            )
            externalDocs = ExternalDocumentation().apply {
                description = "External docs"
                url = "https://example.com"
            }
        }

        assertEquals(expected, openApi)
    }

    @Test
    fun `Test with nothing should output a bunch of null`() {
        val context = mockk<OpenApiDslContext>()
        val openApi = RootBuilder(context).build()

        val expected = OpenAPI()

        assertEquals(expected, openApi)
    }
}
