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
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.responses.ApiResponse

/**
 * DSL for [response objects][https://spec.openapis.org/oas/v3.1.0#response-object].
 */
@TegralDsl
interface ResponseDsl : BodyDsl {
    /**
     * A description of the response. CommonMark syntax may be used for rich text representation.
     */
    @TegralDsl
    var description: String?

    /**
     * Headers this response may return.
     */
    val headers: MutableList<Pair<String, Buildable<Header>>>

    /**
     * Add a header to this response with the given string as its name.
     */
    infix fun String.header(builder: HeaderBuilder.() -> Unit)
}

/**
 * Builder for Response objects, implementing the [ResponseDsl].
 */
class ResponseBuilder(context: OpenApiDslContext) : BodyBuilder(context), ResponseDsl, Buildable<ApiResponse> {
    override var description: String? = null
    override val headers: MutableList<Pair<String, Buildable<Header>>> = mutableListOf()

    override infix fun String.header(builder: HeaderBuilder.() -> Unit) {
        headers += this to HeaderBuilder(context).apply(builder)
    }

    override fun build(): ApiResponse = ApiResponse().apply {
        description(this@ResponseBuilder.description)
        if (this@ResponseBuilder.headers.isNotEmpty()) {
            headers(this@ResponseBuilder.headers.associate { (name, builder) -> name to builder.build() })
        }
        if (this@ResponseBuilder.content.isNotEmpty()) {
            content = Content()
            for ((typeString, typeBuilder) in this@ResponseBuilder.content) {
                content.addMediaType(typeString, typeBuilder.build())
            }
        }
    }
}
