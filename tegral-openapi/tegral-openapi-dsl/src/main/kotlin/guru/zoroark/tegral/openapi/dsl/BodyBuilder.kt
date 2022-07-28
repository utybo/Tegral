package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse

data class ContentType(val contentType: String) {
    infix fun or(other: ContentType): MultiContentType = MultiContentType(listOf(this, other))
}

data class MultiContentType(val types: List<ContentType>) {
    infix fun or(other: ContentType): MultiContentType = MultiContentType(types + other)
}

interface PredefinedContentTypesDsl {
    val xml get() = ContentType("application/xml")
    val json get() = ContentType("application/json")
    val form get() = ContentType("application/x-www-form-urlencoded")
}

interface BodyDsl : PredefinedContentTypesDsl {
    infix fun String.content(builder: MediaTypeBuilder.() -> Unit)

    operator fun ContentType.invoke(builder: MediaTypeBuilder.() -> Unit) {
        contentType content builder
    }

    operator fun MultiContentType.invoke(builder: MediaTypeBuilder.() -> Unit) {
        types.forEach { it(builder) }
    }
}

interface RequestBodyDsl {
    var required: Boolean?
}

@KoaDsl
abstract class BodyBuilder(protected val context: KoaDslContext) : PartialBodyDsl, BodyDsl {
    override var description: String? = null
    val contentTypes = mutableListOf<Pair<String, Builder<MediaType>>>()

    @KoaDsl
    override infix fun String.content(builder: MediaTypeBuilder.() -> Unit) {
        contentTypes.add(this to MediaTypeBuilder(context).apply(builder))
    }
}

@KoaDsl
class ResponseBuilder(context: KoaDslContext) : BodyBuilder(context), Builder<ApiResponse> {
    override fun build(): ApiResponse = ApiResponse().apply {
        description(this@ResponseBuilder.description)
        if (contentTypes.isNotEmpty()) {
            content = Content().apply {
                for ((typeString, typeBuilder) in contentTypes) {
                    addMediaType(typeString, typeBuilder.build())
                }
            }
        }
    }
}

class RequestBodyBuilder(context: KoaDslContext) : BodyBuilder(context), RequestBodyDsl, Builder<RequestBody> {
    override var required: Boolean? = null
    override fun build(): RequestBody = RequestBody().apply {
        description = this@RequestBodyBuilder.description
        required = this@RequestBodyBuilder.required
        if (contentTypes.isNotEmpty()) {
            content = Content().apply {
                for ((typeString, typeBuilder) in contentTypes) {
                    addMediaType(typeString, typeBuilder.build())
                }
            }
        }
    }
}
