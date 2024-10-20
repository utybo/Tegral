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

import kotlin.test.Test
import kotlin.test.assertEquals

class OpenApiConversionTest {
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
            """
            {"openapi":"3.1.0","info":{"title":"My API","version":"0.0.0"},
            "jsonSchemaDialect":"https://spec.openapis.org/oas/3.1/dialect/base"}
            """.trimIndent().replace("\n", ""),
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
            jsonSchemaDialect: https://spec.openapis.org/oas/3.1/dialect/base
            """.trimIndent() + "\n",
            yaml
        )
    }
}
