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
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.parameters.RequestBody

/**
 * DSL for the [request body object](https://spec.openapis.org/oas/v3.1.0#request-body-object).
 */
@TegralDsl
interface RequestBodyDsl : BodyDsl {
    /**
     * A brief description of the request body. This could contain examples of use. CommonMark syntax may be used for
     * rich text representation.
     */
    @TegralDsl
    var description: String?

    /**
     * Determines if the request body is required in the request. Defaults to false.
     */
    @TegralDsl
    var required: Boolean?
}

/**
 * Builder for the [request body DSL][RequestBodyDsl]
 */
class RequestBodyBuilder(context: OpenApiDslContext) :
    BodyBuilder(context),
    RequestBodyDsl,
    @Suppress("DEPRECATION")
    Builder<RequestBody>,
    Buildable<RequestBody> {
    override var description: String? = null
    override var required: Boolean? = null
    override fun build(): RequestBody = RequestBody().apply {
        description = this@RequestBodyBuilder.description
        required = this@RequestBodyBuilder.required
        if (this@RequestBodyBuilder.content.isNotEmpty()) {
            content = Content()
            for ((typeString, typeBuilder) in this@RequestBodyBuilder.content) {
                content.addMediaType(typeString, typeBuilder.build())
            }
        }
    }
}
