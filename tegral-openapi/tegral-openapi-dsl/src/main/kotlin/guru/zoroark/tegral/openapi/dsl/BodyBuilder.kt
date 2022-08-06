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

import guru.zoroark.tegral.core.TegralDsl
import io.swagger.v3.oas.models.media.MediaType

/**
 * DSL for body-like objects, i.e. objects that contain a "content" property.
 */
@TegralDsl
interface BodyDsl : PredefinedContentTypesDsl {
    /**
     * A "map" containing descriptions of potential payloads. The key is a media type or media type range and teh value
     * describes it.
     *
     * Note: this is provided as a MutableList in order to maintain the order content types were defined in.
     */
    @TegralDsl
    val content: MutableList<Pair<String, Builder<MediaType>>>

    /**
     * Creates a content entry for this content type (the string receiver) and body (the builder configured by the
     * lambda). See [MediaTypeDsl] for more information on what you can do in the lambda.
     */
    @TegralDsl
    infix fun String.content(builder: MediaTypeBuilder.() -> Unit)

    /**
     * Creates a content entry for this content type and body. See [MediaTypeDsl] for more information on what you can
     * do in the lambda.
     */
    @TegralDsl
    operator fun ContentType.invoke(builder: MediaTypeBuilder.() -> Unit) {
        contentType content builder
    }

    /**
     * Creates content entries for each of the provided content types with the same body. See [MediaTypeDsl] for more
     * information on what you can do in the lambda.
     */
    @TegralDsl
    operator fun MultiContentType.invoke(builder: MediaTypeBuilder.() -> Unit) {
        types.forEach { it(builder) }
    }
}

/**
 * A default builder for anything that needs to implement [BodyDsl].
 *
 * Should not be used as is and should be subclassed by builders that also wish to include a [body DSL][BodyDsl].
 */
@TegralDsl
abstract class BodyBuilder(protected val context: OpenApiDslContext) : BodyDsl {
    override val content = mutableListOf<Pair<String, Builder<MediaType>>>()

    @TegralDsl
    override infix fun String.content(builder: MediaTypeBuilder.() -> Unit) {
        content.add(this to MediaTypeBuilder(context).apply(builder))
    }
}
