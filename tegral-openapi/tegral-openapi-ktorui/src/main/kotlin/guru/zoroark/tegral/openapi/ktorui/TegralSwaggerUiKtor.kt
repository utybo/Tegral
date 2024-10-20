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

package guru.zoroark.tegral.openapi.ktorui

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.defaultForFileExtension
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.plugin
import io.ktor.server.http.content.resourceClasspathResource
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.server.routing.get
import io.ktor.util.AttributeKey
import io.ktor.utils.io.InternalAPI
import java.util.Properties

private const val SWAGGER_UI_POM_LOCATION: String = "META-INF/maven/org.webjars/swagger-ui/pom.properties"

/**
 * Feature that provides support for Swagger UI in Ktor applications.
 *
 * By itself, this feature does not do anything useful for end-users (it mostly maintains internal state). Use
 * [swaggerUiEndpoint] to serve Swagger UI from a given endpoint.
 */
class TegralSwaggerUiKtor(private val classpathPath: String) {
    /**
     * The configuration class -- because the [TegralSwaggerUiKtor] plugin does not actually have anything to configure,
     * this class is empty.
     */
    class Configuration

    /**
     * Plugin object for [TegralSwaggerUiKtor].
     */
    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, TegralSwaggerUiKtor> {
        override val key = AttributeKey<TegralSwaggerUiKtor>("KoaSwaggerUi")

        private fun computeSwaggerUiPath(): String {
            val properties =
                TegralSwaggerUiKtor::class.java.classLoader.getResourceAsStream(SWAGGER_UI_POM_LOCATION)
                    ?.use { Properties().apply { load(it) } }
                    ?: error("Failed to load, is org.webjars:swagger-ui on classpath?")
            val version = properties["version"]
            return "META-INF/resources/webjars/swagger-ui/$version"
        }

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): TegralSwaggerUiKtor {
            val swaggerUiPath = computeSwaggerUiPath()
            return TegralSwaggerUiKtor(swaggerUiPath)
        }
    }

    private fun getPathFor(fileName: String) = "$classpathPath/$fileName"

    @OptIn(InternalAPI::class)
    internal fun getContentFor(fileName: String): OutgoingContent? {
        val path = getPathFor(fileName)
        val resource = TegralSwaggerUiKtor::class.java.classLoader.getResource(path) ?: return null
        return resourceClasspathResource(
            resource,
            path
        ) { ContentType.defaultForFileExtension(it.path.extension()) }
    }
}

// Taken from Ktor's StaticContentResolution (which is internal so we can't reuse it)
private fun String.extension(): String {
    val indexOfName = lastIndexOf('/').takeIf { it != -1 } ?: lastIndexOf('\\').takeIf { it != -1 } ?: 0
    val indexOfDot = indexOf('.', indexOfName)
    return if (indexOfDot >= 0) substring(indexOfDot) else ""
}

/**
 * Sets up required endpoints for serving Swagger UI.
 *
 * Swagger UI will be served from the given path.
 *
 * **You must call this from the root `routing` block, as the path is considered to be the full path from the root of
 * your API.**
 *
 * @param path The path from which Swagger UI will be served
 * @param openApiPath The path from which Swagger UI will grab the OpenAPI document. Can also be a URL.
 */
fun Route.swaggerUiEndpoint(path: String, openApiPath: String) {
    get("$path/swagger-initializer.js") {
        call.respondText(
            """
            window.onload = function() {
              window.ui = SwaggerUIBundle({
                url: "$openApiPath",
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [
                  SwaggerUIBundle.presets.apis,
                  SwaggerUIStandalonePreset
                ],
                plugins: [
                  SwaggerUIBundle.plugins.DownloadUrl
                ],
                layout: "StandaloneLayout"
              });
            };
            """.trimIndent(),
            ContentType.Application.JavaScript
        )
    }

    get("$path/{fileName?}") {
        val fileName = call.parameters["fileName"] ?: "index.html"
        val result = application.plugin(TegralSwaggerUiKtor).getContentFor(fileName)
        if (result != null) {
            call.respond(result)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    get(path) {
        call.respondRedirect("$path/index.html")
    }
}
