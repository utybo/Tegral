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

import io.swagger.v3.oas.models.security.SecurityScheme
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SecuritySchemeTest {
    @Test
    fun `HTTP security scheme`() {
        val securityScheme = SecuritySchemeBuilder().apply {
            httpType
            description = "My HTTP security scheme"
            scheme = "Bearer"
            bearerFormat = "JWT"
        }

        val expected = SecurityScheme().apply {
            type = SecurityScheme.Type.HTTP
            description = "My HTTP security scheme"
            scheme = "Bearer"
            bearerFormat = "JWT"
        }

        assertEquals(expected, securityScheme.build())
    }

    private fun createBaseApiKeyScheme(block: SecuritySchemeDsl.() -> Unit) = SecuritySchemeBuilder().apply {
        apiKeyType
        description = "My API Key security scheme"
        name = "api_key"
        block()
    }

    @Test
    fun `API Key in query security scheme`() {
        val securityScheme = createBaseApiKeyScheme {
            inQuery
        }

        val expected = SecurityScheme().apply {
            type = SecurityScheme.Type.APIKEY
            `in` = SecurityScheme.In.QUERY
            description = "My API Key security scheme"
            name = "api_key"
        }

        assertEquals(expected, securityScheme.build())
    }

    @Test
    fun `API Key in header security scheme`() {
        val securityScheme = createBaseApiKeyScheme {
            inHeader
        }

        val expected = SecurityScheme().apply {
            type = SecurityScheme.Type.APIKEY
            `in` = SecurityScheme.In.HEADER
            description = "My API Key security scheme"
            name = "api_key"
        }

        assertEquals(expected, securityScheme.build())
    }

    @Test
    fun `API Key in cookie security scheme`() {
        val securityScheme = createBaseApiKeyScheme {
            inCookie
        }

        val expected = SecurityScheme().apply {
            type = SecurityScheme.Type.APIKEY
            `in` = SecurityScheme.In.COOKIE
            description = "My API Key security scheme"
            name = "api_key"
        }

        assertEquals(expected, securityScheme.build())
    }

    @Test
    fun `Mutual TLS security scheme`() {
        val securityScheme = SecuritySchemeBuilder().apply {
            mutualTlsType
            description = "My TLS security scheme"
        }

        val expected = SecurityScheme().apply {
            type = SecurityScheme.Type.MUTUALTLS
            description = "My TLS security scheme"
        }

        assertEquals(expected, securityScheme.build())
    }

    @Test
    fun `Oauth2 security scheme`() {
        val securityScheme = SecuritySchemeBuilder().apply {
            oauth2Type
            description = "My OAuth 2 security scheme"
        }

        val expected = SecurityScheme().apply {
            type = SecurityScheme.Type.OAUTH2
            description = "My OAuth 2 security scheme"
        }

        assertEquals(expected, securityScheme.build())
    }

    @Test
    fun `OpenID security scheme`() {
        val securityScheme = SecuritySchemeBuilder().apply {
            openIdConnectType
            description = "My OIDC security scheme"
            openIdConnectUrl = "https://example.org"
        }

        val expected = SecurityScheme().apply {
            type = SecurityScheme.Type.OPENIDCONNECT
            description = "My OIDC security scheme"
            openIdConnectUrl = "https://example.org"
        }

        assertEquals(expected, securityScheme.build())
    }
}
