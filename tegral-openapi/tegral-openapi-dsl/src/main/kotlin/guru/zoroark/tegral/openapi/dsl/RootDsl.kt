package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.servers.Server

interface RootDsl : InfoDsl, TagsDsl, PathsDsl {
    infix fun String.securityScheme(scheme: SecuritySchemeDsl.() -> Unit)

    fun servers(servers: ServersDsl.() -> Unit)

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
    private val servers = mutableListOf<Builder<List<Server>>>()

    override var externalDocsDescription: String? = null
    override var externalDocsUrl: String? = null

    override fun String.tag(tagBuilder: TagDsl.() -> Unit) {
        tags += TagBuilder(this).apply(tagBuilder)
    }

    override infix fun String.securityScheme(scheme: SecuritySchemeDsl.() -> Unit) {
        securitySchemes[this] = SecuritySchemeBuilder().apply(scheme)
    }

    override fun servers(servers: ServersDsl.() -> Unit) {
        val serversBuilder = ServersBuilder().apply(servers)
        this.servers.add { serversBuilder.build() }
    }

    override fun build(): OpenAPI = OpenAPI().apply {
        tags = this@RootBuilder.tags.map { it.build() }
        info = infoBuilder.build()
        if (securitySchemes.isNotEmpty()) {
            if (components == null) components = Components()
            securitySchemes.forEach { (key, securityScheme) ->
                components.addSecuritySchemes(key, securityScheme.build())
            }
        }
        paths = this@RootBuilder.paths.build()

        if (externalDocsUrl != null || externalDocsDescription != null) {
            externalDocs = ExternalDocumentation().apply {
                url = externalDocsUrl
                description = externalDocsDescription
            }
        }

        servers = this@RootBuilder.servers.flatMap { it.build() }
    }
}
