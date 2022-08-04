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

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server

interface RootDsl : InfoDsl, TagsDsl, PathsDsl {
    infix fun String.securityScheme(scheme: SecuritySchemeDsl.() -> Unit)

    infix fun String.server(server: ServerDsl.() -> Unit)

    var externalDocsDescription: String?
    var externalDocsUrl: String?
}

class RootBuilder(
    private val context: KoaDslContext,
    private val infoBuilder: InfoBuilder = InfoBuilder(),
    private val paths: PathsBuilder = PathsBuilder(context),
    private val securitySchemes: MutableMap<String, SecuritySchemeBuilder> = mutableMapOf()
) : RootDsl, InfoDsl by infoBuilder, PathsDsl by paths, Builder<OpenAPI> {
    private val tags = mutableListOf<TagBuilder>()
    private val servers = mutableListOf<Builder<Server>>()

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

        servers = this@RootBuilder.servers.map { it.build() }.ifEmpty { null }
    }
}
