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

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.converter.ModelConverterContextImpl
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Json31
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.core.util.Yaml31
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
    // from the model converter which returns null when encountering "top-level" collections, e.g. List<Foo>)
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

    override fun persistTo(openApi: OpenAPI) {
        if (context.definedModels.isNotEmpty()) {
            if (openApi.components == null) openApi.components = Components()

            for ((name, schema) in context.definedModels.entries.sortedBy { it.key }) {
                openApi.components.addSchemas(name, schema)
            }
        }
    }
}

@KoaDsl
fun openApi(builder: RootBuilder.() -> Unit): OpenAPI {
    val context = SimpleDslContext()
    val root = RootBuilder(context)
    root.builder()

    val openApi = root.build()
    context.persistTo(openApi)
    return openApi
}

enum class OpenApiVersion(val version: String) {
    V3_0("3.0"),
    V3_1("3.1")
}

fun OpenAPI.toJson(version: OpenApiVersion = OpenApiVersion.V3_0): String = when (version) {
    OpenApiVersion.V3_0 -> Json.mapper().writeValueAsString(this)
    OpenApiVersion.V3_1 -> Json31.mapper().writeValueAsString(this)
}

fun OpenAPI.toYaml(version: OpenApiVersion = OpenApiVersion.V3_0): String = when (version) {
    OpenApiVersion.V3_0 -> Yaml.pretty(this)
    OpenApiVersion.V3_1 -> Yaml31.pretty(this)
}
