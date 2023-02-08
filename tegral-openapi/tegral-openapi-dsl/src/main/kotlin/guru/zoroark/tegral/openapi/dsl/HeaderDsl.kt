package guru.zoroark.tegral.openapi.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType

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

class HeaderBuilder(private val context: OpenApiDslContext) : HeaderDsl, @Suppress("DEPRECATION") Builder<Header>, Buildable<Header> {
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
