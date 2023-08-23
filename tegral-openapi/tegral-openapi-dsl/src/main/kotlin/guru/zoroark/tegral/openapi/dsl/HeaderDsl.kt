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
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType

/**
 * DSL interface for response headers
 */
@TegralDsl
interface HeaderDsl : MediaTypeDsl {
    /**
     * A brief description of the header. This could contain examples of use. CommonMark syntax may be used for
     * rich text representation.
     */
    @TegralDsl
    var description: String?

    /**
     * Specifies that a header is deprecated and should be transitioned out of usage. False by default.
     */
    @TegralDsl
    var deprecated: Boolean?

    /**
     * When set to true, parameter values of type `array` or `object` generate separate parameters for each value of
     * the array or key-value pair of the map. Has no effect for other types of parameters. Default value is `false`.
     */
    @TegralDsl
    var explode: Boolean?
}

/**
 * Builder for [ResponseDsl]
 */
class HeaderBuilder(private val context: OpenApiDslContext) : HeaderDsl, Buildable<Header> {
    override var description: String? = null
    override var deprecated: Boolean? = null
    override var explode: Boolean? = null
    override var schema: Schema<*>? = null
    override var example: Any? = null

    override fun schema(ktype: KType) {
        schema = context.computeAndRegisterSchema(ktype)
    }

    override fun build(): Header {
        val header = Header()

        description?.let { header.description(it) }
        deprecated?.let { header.deprecated(it) }
        explode?.let { header.explode(it) }
        schema?.let { header.schema(it) }
        example?.let { header.example(it) }

        return header
    }
}
