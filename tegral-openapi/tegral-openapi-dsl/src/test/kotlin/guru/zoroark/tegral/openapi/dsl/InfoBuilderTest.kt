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

import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import kotlin.test.Test
import kotlin.test.assertEquals

class InfoBuilderTest {
    @Test
    fun `Build full object`() {
        val info = InfoBuilder().apply {
            title = "My API"
            version = "1.0"
            summary = "My API summary"
            description = "This is my API"
            termsOfService = "https://example.com/terms"
            contactName = "John Doe"
            contactEmail = "john.doe@example.org"
            contactUrl = "https://example.com/contact"
            licenseName = "MIT"
            licenseIdentifier = "MIT"
            // Technically invalid because we are also providing a licenseIdentifier
            licenseUrl = "https://example.com/license"
        }.build()
        val expected = Info().apply {
            title = "My API"
            version = "1.0"
            summary = "My API summary"
            description = "This is my API"
            termsOfService = "https://example.com/terms"
            contact = Contact().apply {
                name = "John Doe"
                email = "john.doe@example.org"
                url = "https://example.com/contact"
            }
            license = License().apply {
                name = "MIT"
                identifier = "MIT"
                url = "https://example.com/license"
            }
        }
        assertEquals(info, expected)
    }

    private fun testContact(contactName: String?, contactEmail: String?, contactUrl: String?) {
        val info = InfoBuilder().apply {
            title = "My API"

            this.contactName = contactName
            this.contactEmail = contactEmail
            this.contactUrl = contactUrl
        }

        val expected = Info().apply {
            title = "My API"
            contact = Contact().apply {
                name = contactName
                email = contactEmail
                url = contactUrl
            }
        }

        assertEquals(expected, info.build())
    }

    @Test
    fun `Test contact when some missing`() {
        testContact("John Doe", null, null)
        testContact(null, "john.doe@example.com", null)
        testContact(null, null, "https://example.com/contact")
    }

    private fun testLicense(licenseName: String?, licenseIdentifier: String?, licenseUrl: String?) {
        val info = InfoBuilder().apply {
            title = "My API"

            this.licenseName = licenseName
            this.licenseIdentifier = licenseIdentifier
            this.licenseUrl = licenseUrl
        }

        val expected = Info().apply {
            title = "My API"
            license = License().apply {
                name = licenseName
                identifier = licenseIdentifier
                url = licenseUrl
            }
        }

        assertEquals(expected, info.build())
    }

    @Test
    fun `Test license when some missing`() {
        testLicense("MIT", null, null)
        testLicense(null, "MIT", null)
        testLicense(null, null, "https://example.com/license")
    }
}
