package guru.zoroark.tegral.openapi.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class OpenApiConversion {
    @Test
    fun `toJson v3_0`() {
        val document = openApi {
            title = "My API"
            version = "0.0.0"
        }
        val json = document.toJson()
        assertEquals("""{"openapi":"3.0.1","info":{"title":"My API","version":"0.0.0"}}""", json)
    }

    @Test
    fun `toJson v3_1`() {
        val document = openApi {
            title = "My API"
            version = "0.0.0"
        }
        val json = document.toJson(OpenApiVersion.V3_1)
        assertEquals(
            """{"openapi":"3.1.0","info":{"title":"My API","version":"0.0.0"},"jsonSchemaDialect":"https://json-schema.org/draft/2020-12/schema"}""",
            json
        )
    }

    @Test
    fun `toYaml v3_0`() {
        val document = openApi {
            title = "My API"
            version = "0.0.0"
        }
        val yaml = document.toYaml()
        assertEquals(
            """
            openapi: 3.0.1
            info:
              title: My API
              version: 0.0.0
            """.trimIndent() + "\n",
            yaml
        )
    }

    @Test
    fun `toYaml v3_1`() {
        val document = openApi {
            title = "My API"
            version = "0.0.0"
        }
        val yaml = document.toYaml(OpenApiVersion.V3_1)
        assertEquals(
            """
            openapi: 3.1.0
            info:
              title: My API
              version: 0.0.0
            jsonSchemaDialect: https://json-schema.org/draft/2020-12/schema
            """.trimIndent() + "\n",
            yaml
        )
    }
}
