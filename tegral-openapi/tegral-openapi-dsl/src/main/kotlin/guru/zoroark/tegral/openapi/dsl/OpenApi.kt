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

import io.swagger.v3.core.util.Json
import io.swagger.v3.core.util.Json31
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.core.util.Yaml31
import io.swagger.v3.oas.models.OpenAPI

/**
 * Entrypoint for the Tegral OpenAPI DSL.
 *
 * This takes a lambda with the [RootDsl], from which you can do everything offered by the DSL. This function creates a
 * [DSL contxt][KoaDslContext] calls the lambda, builds the OpenAPI object and persists the context.
 */
@KoaDsl
fun openApi(builder: RootBuilder.() -> Unit): OpenAPI {
    val context = SimpleDslContext()
    val root = RootBuilder(context)
    root.builder()

    val openApi = root.build()
    context.persistTo(openApi)
    return openApi
}

/**
 * Dumps this OpenAPI object to a JSON string.
 */
fun OpenAPI.toJson(version: OpenApiVersion = OpenApiVersion.V3_0): String = when (version) {
    OpenApiVersion.V3_0 -> Json.mapper().writeValueAsString(this)
    OpenApiVersion.V3_1 -> Json31.mapper().writeValueAsString(this)
}

/**
 * Dumps this OpenAPI object to a prettified YAML string.
 */
fun OpenAPI.toYaml(version: OpenApiVersion = OpenApiVersion.V3_0): String = when (version) {
    OpenApiVersion.V3_0 -> Yaml.pretty(this)
    OpenApiVersion.V3_1 -> Yaml31.pretty(this)
}
