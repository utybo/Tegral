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

/**
 * DSL for the [info object](https://spec.openapis.org/oas/v3.1.0#info-object).
 *
 * Note that the `contact` and `license` fields are directly embedded in this object.
 */
interface InfoDsl {
    /**
     * The title of the API.
     */
    var title: String?

    /**
     * A short summary of the API.
     */
    var summary: String?

    /**
     * A short summary of the API.
     */
    var description: String?

    /**
     * A URL to the Terms of Service for the API. This must be in the form of a URL.
     */
    var termsOfService: String?

    /**
     * The identifying name of the contact person/organization for the exposed API.
     */
    var contactName: String?

    /**
     * The URL pointing to the contact information for the exposed API. This must be in the form of a URL.
     */
    var contactUrl: String?

    /**
     * The email address of the contact person/organization for the exposed API. THis must be in the form of an email
     * address.
     */
    var contactEmail: String?

    /**
     * The license name used for the API.
     */
    var licenseName: String?

    /**
     * An [SPDX](https://spdx.org/) license identifier for the API.
     *
     * The `licenseIdentifier` field is mutually exclusive of the [licenseUrl] field.
     */
    var licenseIdentifier: String?

    /**
     * A URL to the license used for the API. This must be in the form of a URL.
     *
     * The `licenseUrl` field is mutually exclusive of the [licenseIdentifier] field.
     */
    var licenseUrl: String?

    /**
     * The version of the OpenAPI document. This is not the same as the version of the OpenAPI specification this
     * document follows.
     */
    var version: String?
}

/**
 * Builder object for the [info object](https://spec.openapis.org/oas/v3.1.0#info-object).
 */
@KoaDsl
class InfoBuilder : Builder<Info>, InfoDsl {
    override var title: String? = null
    override var summary: String? = null
    override var description: String? = null
    override var termsOfService: String? = null
    override var version: String? = null

    override var contactName: String? = null
    override var contactUrl: String? = null
    override var contactEmail: String? = null

    override var licenseName: String? = null
    override var licenseUrl: String? = null
    override var licenseIdentifier: String? = null

    override fun build(): Info = Info().apply {
        title = this@InfoBuilder.title
        summary = this@InfoBuilder.summary
        description = this@InfoBuilder.description
        termsOfService = this@InfoBuilder.termsOfService
        version = this@InfoBuilder.version

        if (contactName != null || contactUrl != null || contactEmail != null) {
            contact = Contact().apply {
                name = contactName
                url = contactUrl
                email = contactEmail
            }
        }

        if (licenseName != null || licenseUrl != null || licenseIdentifier != null) {
            license = License().apply {
                name = licenseName
                url = licenseUrl
                identifier = licenseIdentifier
            }
        }
    }
}
