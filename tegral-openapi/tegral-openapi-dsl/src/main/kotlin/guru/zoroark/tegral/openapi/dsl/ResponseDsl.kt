package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.responses.ApiResponse

/**
 * DSL for [response objects][https://spec.openapis.org/oas/v3.1.0#response-object].
 */
interface ResponseDsl : BodyDsl {
    /**
     * A description of the response. CommonMark syntax may be used for rich text representation.
     */
    var description: String?
    // TODO headers, links
}

/**
 * Builder for Response objects, implementing the [ResponseDsl].
 */
@KoaDsl
class ResponseBuilder(context: KoaDslContext) : BodyBuilder(context), ResponseDsl, Builder<ApiResponse> {
    override var description: String? = null
    override fun build(): ApiResponse = ApiResponse().apply {
        description(this@ResponseBuilder.description)
        if (this@ResponseBuilder.content.isNotEmpty()) {
            content = Content().apply {
                for ((typeString, typeBuilder) in this@ResponseBuilder.content) {
                    addMediaType(typeString, typeBuilder.build())
                }
            }
        }
    }
}
