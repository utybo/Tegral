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

package guru.zoroark.tegral.openapi.ktor

import io.ktor.server.application.MissingApplicationPluginException
import io.ktor.server.testing.testApplication
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PluginTest {
    @Test
    fun `Plugin retrieval without installation`() {
        testApplication {
            application {
                assertFailsWith<MissingApplicationPluginException> { this.openApi }
            }
        }
    }

    @Test
    fun `Plugin installation and retrieval`() {
        testApplication {
            install(TegralOpenApiKtor) {
            }

            application {
                assertDoesNotThrow { this.openApi }
            }
        }
    }

    @Test
    fun `Create OpenAPI document with only basic info`() {
        testApplication {
            install(TegralOpenApiKtor) {
                title = "My API"
                version = "0.0.0"

                summary = "This is my API"
            }

            application {
                val document = openApi.buildOpenApiDocument()
                val expected = OpenAPI().apply {
                    info = Info().apply {
                        title = "My API"
                        version = "0.0.0"
                        summary = "This is my API"
                    }
                }
                assertEquals(expected, document)
            }
        }
    }

    @Test
    fun `Add security requirements on root`() {
        testApplication {
            install(TegralOpenApiKtor) {
                security("scheme1")
                security("scheme2", "scope1", "scope2")
                security {
                    requirement("scheme3")
                    requirement("scheme4", "scope3")
                }
            }

            application {
                val document = openApi.buildOpenApiDocument()
                val expected = OpenAPI().apply {
                    security = listOf(
                        SecurityRequirement().addList("scheme1"),
                        SecurityRequirement().addList("scheme2", listOf("scope1", "scope2")),
                        SecurityRequirement().addList("scheme3").addList("scheme4", listOf("scope3")),
                    )
                }
                assertEquals(expected, document)
            }
        }
    }
}
