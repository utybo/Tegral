package guru.zoroark.tegral.openapi.feature

import guru.zoroark.tegral.openapi.dsl.openApi
import guru.zoroark.tegral.openapi.ktor.openApi
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDescriptionTest {
    @Test
    fun `Default description`() {
        testApplication {
            application {
                with(OpenApiModule()) { install() }
                val doc = openApi.buildOpenApiDocument()
                assertEquals("Tegral application", doc.info.title)
                assertEquals(
                    "*This [Tegral](https://tegral.zoroark.guru) application does not provide a description.*",
                    doc.info.description
                )
                assertEquals("0.0.0", doc.info.version)
            }
        }
    }
}
