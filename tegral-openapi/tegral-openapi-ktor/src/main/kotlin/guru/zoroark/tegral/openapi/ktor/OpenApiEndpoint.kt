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

package guru.zoroark.tegral.openapi.ktor

import guru.zoroark.tegral.openapi.dsl.OpenApiVersion
import guru.zoroark.tegral.openapi.dsl.toJson
import guru.zoroark.tegral.openapi.dsl.toYaml
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.swagger.v3.oas.models.OpenAPI

private fun parseVersion(version: String?): OpenApiVersion? =
    if (version == null) {
        OpenApiVersion.V3_0
    } else {
        OpenApiVersion.entries.firstOrNull { it.version == version }
    }

private enum class Format(
    val value: String,
    val contentType: ContentType,
    val transformer: OpenAPI.(OpenApiVersion) -> String
) {
    JSON("json", ContentType.Application.Json, OpenAPI::toJson),
    YAML("yaml", ContentType("text", "yaml"), OpenAPI::toYaml)
}

private fun parseFormat(format: String?): Format? =
    if (format == null) {
        Format.JSON
    } else {
        Format.entries.firstOrNull { it.value == format }
    }

/**
 * Adds a `get(path)` endpoint that will return the OpenAPI document for this API.
 *
 * Available query parameters are:
 *
 * - `version`: the version of the OpenAPI document to return (supported values: `3.0` (default), `3.1`)
 * - `format`: the format of the OpenAPI document to return (supported values: `json` (default), `yaml`)
 *
 * You must install the [TegralOpenApiKtor] feature and [describe][describe] your endpoints for this to work.
 */
fun Route.openApiEndpoint(path: String): Route = get(path) {
    val format = parseFormat(call.request.queryParameters["format"])
    if (format == null) {
        call.respondText("Invalid format", ContentType.Text.Plain, HttpStatusCode.BadRequest)
        return@get
    }
    val version = parseVersion(call.request.queryParameters["version"])
    if (version == null) {
        call.respondText("Invalid version", ContentType.Text.Plain, HttpStatusCode.BadRequest)
        return@get
    }
    val openApi = application.openApi.buildOpenApiDocument()
    val result = format.transformer(openApi, version)
    call.respondText(result, format.contentType, HttpStatusCode.OK)
}
