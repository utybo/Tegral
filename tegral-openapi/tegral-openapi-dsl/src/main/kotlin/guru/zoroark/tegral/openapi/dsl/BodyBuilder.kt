package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.RequestBody

/**
 * DSL for body-like objects, i.e. objects that contain a "content" property.
 */
interface BodyDsl : PredefinedContentTypesDsl {
    /*
     * A "map" containing descriptions of potential payloads. The key is a media type or media type range and teh value
     * describes it.
     *
     * Note: this is provided as a MutableList in order to maintain the order content types were defined in.
     */
    val content: MutableList<Pair<String, Builder<MediaType>>>

    /**
     * Creates a content entry for this content type (the string receiver) and body (the builder configured by the
     * lambda). See [MediaTypeDsl] for more information on what you can do in the lambda.
     */
    infix fun String.content(builder: MediaTypeBuilder.() -> Unit)

    /**
     * Creates a content entry for this content type and body. See [MediaTypeDsl] for more information on what you can
     * do in the lambda.
     */
    operator fun ContentType.invoke(builder: MediaTypeBuilder.() -> Unit) {
        contentType content builder
    }

    /*
     * Creates content entries for each of the provided content types with the same body. See [MediaTypeDsl] for more
     * information on what you can do in the lambda.
     */
    operator fun MultiContentType.invoke(builder: MediaTypeBuilder.() -> Unit) {
        types.forEach { it(builder) }
    }
}

/**
 * A default builder for anything that needs to implement [BodyDsl].
 *
 * Should not be used as is and should be subclassed by builders that also wish to include a [body DSL][BodyDsl].
 */
@KoaDsl
abstract class BodyBuilder(protected val context: KoaDslContext) : BodyDsl {
    override val content = mutableListOf<Pair<String, Builder<MediaType>>>()

    @KoaDsl
    override infix fun String.content(builder: MediaTypeBuilder.() -> Unit) {
        content.add(this to MediaTypeBuilder(context).apply(builder))
    }
}
