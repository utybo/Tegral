package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType
import kotlin.reflect.typeOf

/**
 * DSL for [media type objects](https://spec.openapis.org/oas/v3.1.0#media-type-object
 *
 * This DSL has extensions that allow to easily define schemas from `KType` objects (including types of arbitrary
 * classes).
 */
@KoaDsl
interface MediaTypeDsl {
    /**
     * Sets the schema of this object to be of the given `KType`. The type will be converted to a schema.
     *
     * DSLs implementing `MediaTypeDsl` must properly register any non-standard schema using the [KoaDslContext], which
     * will provide a proper schema with a `$ref`.
     */
    fun schema(ktype: KType)

    /**
     * The schema for this object.
     */
    var schema: Schema<*>?

    /**
     * The example for this object. Should match the given schema.
     */
    var example: Any?
    // TODO examples, encoding
}

/**
 * Set the schema to be of type `T`.
 *
 * The type `T` will be converted to a schema.
 */
@KoaDsl
inline fun <reified T : Any> MediaTypeDsl.schema() = schema(typeOf<T>())

/**
 * Set the schema to be of type `T`, with the given object set as the example.
 *
 * The type `T` will be converted to a schema.
 */
@KoaDsl
inline fun <reified T : Any> MediaTypeDsl.schema(example: T) {
    this.example = example
    schema(typeOf<T>())
}

/**
 * Set the schema to be of type [ktype], with the given object set as the example.
 */
@KoaDsl
fun <T> MediaTypeDsl.schema(ktype: KType, example: T) {
    this.example = example
    schema(ktype)
}

/**
 * A builder for [MediaTypeDsl].
 */
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
