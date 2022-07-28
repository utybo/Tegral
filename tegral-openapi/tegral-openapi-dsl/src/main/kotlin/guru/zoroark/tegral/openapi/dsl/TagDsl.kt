package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.tags.Tag

interface TagDsl {
    var description: String?
    var externalDocsDescription: String?
    var externalDocsUrl: String?
}

class TagBuilder(private val name: String) : TagDsl, Builder<Tag> {
    override var description: String? = null
    override var externalDocsDescription: String? = null
    override var externalDocsUrl: String? = null

    override fun build(): Tag = Tag().apply {
        name = this@TagBuilder.name
        description = this@TagBuilder.description
        if (externalDocsDescription != null || externalDocsUrl != null) {
            externalDocs = ExternalDocumentation().apply {
                description = externalDocsDescription
                url = externalDocsUrl
            }
        }
    }
}
