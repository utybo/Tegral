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
        val context = mockk<KoaDslContext>()
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
        val context = mockk<KoaDslContext>()
        val openApi = RootBuilder(context).build()

        val expected = OpenAPI()

        assertEquals(expected, openApi)
    }
}
