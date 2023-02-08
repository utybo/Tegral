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
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.CookieParameter
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter
import kotlin.reflect.KType

/**
 * The kind of parameter (i.e. its location).
 *
 * See [here](https://spec.openapis.org/oas/v3.1.0#parameter-locations) for more information on parameter locations.
 */
enum class ParameterKind {
    /**
     * The parameter is a URL query parameter (e.g. the `bar` in `/foo?bar=...`)
     */
    Query,

    /**
     * The parameter is a custom request header.
     */
    Header,

    /**
     * A parameter in a path, for example the `bar` in `/foo/{bar}/baz`.
     */
    Path,

    /**
     * A parameter that is a cookie.
     */
    Cookie
}

/**
 * DSL for [parameter objects][https://spec.openapis.org/oas/v3.1.0#parameter-object]
 */
@TegralDsl
interface ParameterDsl : MediaTypeDsl {
    /**
     * A brief description of the parameter. This could contain examples of use. CommonMark syntax may be used for
     * rich text representation.
     */
    @TegralDsl
    var description: String?

    /**
     * Determines whether this parameter is mandatory. If the parameter is a path parameter, this value will always be
     * set to true, otherwise it is false by default.
     */
    @TegralDsl
    var required: Boolean?

    /**
     * Specifies that a parameter is deprecated and should be transitioned out of usage. False by default.
     */
    @TegralDsl
    var deprecated: Boolean?

    /**
     * If true, the parameter value can be empty. Valid only for query parameters. Default is false.
     */
    @Deprecated(
        "From OpenAPI specifications: \"Use of this property is not recommended and it is likely to be " +
            "removed in a later revision.\""
    )
    @TegralDsl
    var allowEmptyValue: Boolean?

    /**
     * Describes how the parameter value will be serialized depending on the type of the parameter value.
     *
     * See [here](https://spec.openapis.org/oas/v3.1.0#style-values) for more information.
     */
    @TegralDsl
    var style: Parameter.StyleEnum?

    /**
     * When set to true, parameter values of type `array` or `object` generate separate parameters for each value of
     * the array or key-value pair of the map. Has no effect for other types of parameters. Default value is `true` if
     * `style` is set to `form`, `false` otherwise.
     */
    @TegralDsl
    var explode: Boolean?

    /**
     * If true, the parameter value can contain
     * [reserved characters](https://www.rfc-editor.org/rfc/rfc3986#section-2.2).
     *
     * Only applies to query parameters.
     */
    @TegralDsl
    var allowReserved: Boolean?

    /**
     * Sets `required = true` when called.
     */
    @TegralDsl
    fun required() {
        required = true
    }

    // TODO content
}

/**
 * Builder for [ParameterDsl]
 */
class ParameterBuilder(
    private val context: OpenApiDslContext,
    /**
     * The name of the parameter.
     *
     * - If this is a path parameter, it must correspond to a template expression in the path.
     * - If this is a header parameter, its name cannot be `Accept`, `Content-Type` or `Authorization`.
     */
    private val name: String,
    /**
     * The kind of parameter. See [ParameterKind] for more information.
     */
    private val kind: ParameterKind
) : @Suppress("DEPRECATION") Builder<Parameter>, Buildable<Parameter>, ParameterDsl {
    override var description: String? = null
    override var required: Boolean? = null
    override var deprecated: Boolean? = null

    @Deprecated(
        "From OpenAPI specifications: \"Use of this property is not recommended and it is likely to be " +
            "removed in a later revision.\""
    )
    override var allowEmptyValue: Boolean? = null
    override var style: Parameter.StyleEnum? = null
    override var explode: Boolean? = null
    override var allowReserved: Boolean? = null
    override var schema: Schema<*>? = null
    override var example: Any? = null

    override fun schema(ktype: KType) {
        schema = context.computeAndRegisterSchema(ktype)
    }

    override fun build(): Parameter {
        val parameter = when (kind) {
            ParameterKind.Query -> QueryParameter()
            ParameterKind.Cookie -> CookieParameter()
            ParameterKind.Header -> HeaderParameter()
            ParameterKind.Path -> PathParameter()
        }

        parameter.name(name)
        description?.let { parameter.description(it) }
        required?.let { parameter.required(it) }
        deprecated?.let { parameter.deprecated(it) }
        @Suppress("DEPRECATION")
        allowEmptyValue?.let { parameter.allowEmptyValue(it) }
        style?.let { parameter.style(it) }
        explode?.let { parameter.explode(it) }
        allowReserved?.let { parameter.allowReserved(it) }
        schema?.let { parameter.schema(it) }
        example?.let { parameter.example(it) }

        return parameter
    }
}
