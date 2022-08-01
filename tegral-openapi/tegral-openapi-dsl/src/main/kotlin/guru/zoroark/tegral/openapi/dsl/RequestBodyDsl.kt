package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.parameters.RequestBody

/**
 * DSL for the [request body object](https://spec.openapis.org/oas/v3.1.0#request-body-object).
 */
interface RequestBodyDsl : BodyDsl {
    var description: String?
    var required: Boolean?
}

/**
 * Builder for the [request body DSL][RequestBodyDsl]
 */
class RequestBodyBuilder(context: KoaDslContext) : BodyBuilder(context), RequestBodyDsl, Builder<RequestBody> {
    override var description: String? = null
    override var required: Boolean? = null
    override fun build(): RequestBody = RequestBody().apply {
        description = this@RequestBodyBuilder.description
        required = this@RequestBodyBuilder.required
        if (this@RequestBodyBuilder.content.isNotEmpty()) {
            content = Content().apply {
                for ((typeString, typeBuilder) in this@RequestBodyBuilder.content) {
                    addMediaType(typeString, typeBuilder.build())
                }
            }
        }
    }
}
