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
