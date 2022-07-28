package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType
import kotlin.reflect.typeOf

@KoaDsl
interface MediaTypeDsl {
    fun schema(ktype: KType)
    var schema: Schema<*>?
    var example: Any?
}

@KoaDsl
inline fun <reified T : Any> MediaTypeDsl.schema() = schema(typeOf<T>())

@KoaDsl
inline fun <reified T : Any> MediaTypeDsl.schema(example: T) {
    this.example = example
    schema(typeOf<T>())
}

@KoaDsl
fun <T> MediaTypeDsl.schema(ktype: KType, example: T) {
    this.example = example
    schema(ktype)
}

class MediaTypeBuilder(private val context: KoaDslContext) : MediaTypeDsl, Builder<MediaType> {
    override var schema: Schema<*>? = null
    override var example: Any? = null

    override fun build(): MediaType = MediaType().apply {
        example = this@MediaTypeBuilder.example
        schema = this@MediaTypeBuilder.schema
    }

    override fun schema(ktype: KType) {
        context.computeAndRegisterSchema(ktype).also { schema = it }
    }
}
