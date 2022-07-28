package guru.zoroark.tegral.openapi.dsl

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverterContextImpl
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType
import kotlin.reflect.javaType

@OptIn(ExperimentalStdlibApi::class) // javaType is experimental
private fun KType.toJacksonType(mapper: ObjectMapper): JavaType =
    mapper.constructType(javaType)

private fun KType.toAnnotatedTypeAsRef(mapper: ObjectMapper): AnnotatedType {
    val type = toJacksonType(mapper)
    // We want to get a reference in the resulting type, so we'll set resolveAsRef (this also dodges a strange behavior
    // from the model converter which returns null when encountering "top-level" collections)
    return AnnotatedType().type(type).resolveAsRef(true)
}

/**
 * An implementation of [KoaDslContext] that keeps context information for Swagger Core.
 */
class SimpleDslContext : KoaDslContext {
    private val context = ModelConverterContextImpl(ModelConverters.getInstance().converters)

    override fun computeAndRegisterSchema(type: KType): Schema<*> {
        // We're using Json.mapper() here to reflect what ModelConverters uses.
        return context.resolve(type.toAnnotatedTypeAsRef(Json.mapper()))
            ?: error("Could not resolve type $type")
    }

    fun getStoredSchemas(): Map<String, Schema<*>> {
        return context.definedModels
    }
}

@KoaDsl
fun openApi(builder: RootBuilder.() -> Unit): OpenAPI {
    val context = SimpleDslContext()
    val root = RootBuilder(context)
    root.builder()

    val openApi = root.build()
    if (openApi.components == null) {
        openApi.components = Components()
    }

    for ((name, schema) in context.getStoredSchemas().entries.sortedBy { it.key }) {
        openApi.components.addSchemas(name, schema)
    }

    return openApi
}
