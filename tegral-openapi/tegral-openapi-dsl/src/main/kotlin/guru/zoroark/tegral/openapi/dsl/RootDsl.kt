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

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.servers.Server

/**
 * This is the root DSL object for the Tegral OpenAPI DSL.
 *
 * As such, it contains many of the required properties for the OpenAPI object, including:
 *
 * - [Security schemes][securityScheme]
 * - [Servers][server]
 * - [Info][InfoDsl] (embedded)
 * - [Tags][TagsDsl] (embedded)
 * - [Paths][PathsDsl] (embedded)
 * - [Security][SecurityDsl] (embedded)
 * - External documentation ([description][externalDocsDescription] and [url][externalDocsUrl])
 *
 * (Items marked as embedded are separate DSL interfaces that are available in [RootDsl] and can be used directly).
 */
@TegralDsl
interface RootDsl : InfoDsl, TagsDsl, PathsDsl, SecurityDsl {
    /**
     * Adds a security scheme to this OpenAPI document with the given string as the name, using the lambda to configure
     * further options.
     */
    @TegralDsl
    infix fun String.securityScheme(scheme: SecuritySchemeDsl.() -> Unit)

    /**
     * Adds a server to this OpenAPI document with the given string as the URL, using the lambda to configure further
     * options.
     */
    @TegralDsl
    infix fun String.server(server: ServerDsl.() -> Unit)

    /**
     * Description for additional external documentation for this API.
     */
    @TegralDsl
    var externalDocsDescription: String?

    /**
     * URL for additional external documentation for this API.
     */
    @TegralDsl
    var externalDocsUrl: String?
}

/**
 * Builder for the [RootDsl].
 */
class RootBuilder(
    private val context: OpenApiDslContext,
    private val infoBuilder: InfoBuilder = InfoBuilder(),
    private val paths: PathsBuilder = PathsBuilder(context),
    private val securitySchemes: MutableMap<String, SecuritySchemeBuilder> = mutableMapOf()
) : RootDsl, InfoDsl by infoBuilder, PathsDsl by paths, Buildable<OpenAPI> {
    private val tags = mutableListOf<TagBuilder>()
    private val servers = mutableListOf<Buildable<Server>>()
    override var securityRequirements = mutableListOf<SecurityRequirement>()
    override var externalDocsDescription: String? = null
    override var externalDocsUrl: String? = null

    override fun String.tag(tagBuilder: TagDsl.() -> Unit) {
        tags += TagBuilder(this).apply(tagBuilder)
    }

    override infix fun String.securityScheme(scheme: SecuritySchemeDsl.() -> Unit) {
        securitySchemes[this] = SecuritySchemeBuilder().apply(scheme)
    }

    override infix fun String.server(server: ServerDsl.() -> Unit) {
        val serverBuilder = ServerBuilder(this).apply(server)
        servers.add(serverBuilder)
    }

    override fun security(key: String) {
        securityRequirements.add(SecurityRequirement().addList(key))
    }

    override fun security(key: String, vararg scopes: String) {
        securityRequirements.add(SecurityRequirement().addList(key, scopes.toList()))
    }

    override fun security(builder: SecurityRequirementsBuilder.() -> Unit) {
        securityRequirements.add(SecurityRequirementsBuilder().apply(builder).build())
    }

    override fun build(): OpenAPI = OpenAPI().apply {
        tags = this@RootBuilder.tags.map { it.build() }.ifEmpty { null }
        // In case the info part is completely empty, output 'null' to avoid getting an empty, useless object.
        // Otherwise, actually put it properly.
        infoBuilder.build().let {
            info = if (it == Info()) null else it
        }
        if (securitySchemes.isNotEmpty()) {
            if (components == null) components = Components()
            securitySchemes.forEach { (key, securityScheme) ->
                components.addSecuritySchemes(key, securityScheme.build())
            }
        }
        paths = this@RootBuilder.paths.build().ifEmpty { null }

        if (externalDocsUrl != null || externalDocsDescription != null) {
            externalDocs = ExternalDocumentation().apply {
                url = externalDocsUrl
                description = externalDocsDescription
            }
        }
        security = this@RootBuilder.securityRequirements.ifEmpty { null }
        servers = this@RootBuilder.servers.map { it.build() }.ifEmpty { null }
    }
}
