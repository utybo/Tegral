package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.CookieParameter
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter
import kotlin.reflect.KType

enum class ParameterKind {
    Query,
    Header,
    Path,
    Cookie
}

interface ParameterDsl : MediaTypeDsl {
    val name: String
    val kind: ParameterKind

    var description: String?
    var required: Boolean?
    var deprecated: Boolean?
    var allowEmptyValue: Boolean?
    var style: Parameter.StyleEnum?
    var explode: Boolean?
    var allowReserved: Boolean?

    fun required() {
        required = true
    }
}

@KoaDsl
class ParameterBuilder(
    private val context: KoaDslContext,
    override val name: String,
    override val kind: ParameterKind
) : Builder<Parameter>, ParameterDsl {
    override var description: String? = null
    override var required: Boolean? = null
    override var deprecated: Boolean? = null
    override var allowEmptyValue: Boolean? = null
    override var style: Parameter.StyleEnum? = null
    override var explode: Boolean? = null
    override var allowReserved: Boolean? = null
    override var schema: Schema<*>? = null
    override var example: Any? = null

    // TODO content

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
        allowEmptyValue?.let { parameter.allowEmptyValue(it) }
        style?.let { parameter.style(it) }
        explode?.let { parameter.explode(it) }
        allowReserved?.let { parameter.allowReserved(it) }
        schema?.let { parameter.schema(it) }
        example?.let { parameter.example(it) }

        return parameter
    }
}
