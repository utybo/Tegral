package guru.zoroark.tegral.openapi.ktor

import io.ktor.server.application.MissingApplicationPluginException
import io.ktor.server.testing.testApplication
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import kotlin.test.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertFails
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
}
