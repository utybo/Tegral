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

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.dsl.RootDsl
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.pluginOrNull
import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.PathSegmentConstantRouteSelector
import io.ktor.server.routing.PathSegmentParameterRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.KtorDsl
import org.slf4j.LoggerFactory
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean

private val logger = LoggerFactory.getLogger("tegral.openapi.ktor.describe")
private val messageWasPrinted = AtomicBoolean(false)

private fun printNotInstalledWarning(application: Application) {
    if (messageWasPrinted.compareAndSet(false, true)) {
        logger.warn(
            "The TegralOpenApiKtor plugin is not installed in application $application. The 'describe' functions " +
                "will have no effect. This message will only be printed once.\n" +
                "-> Building a Tegral Web application? Use `install(OpenApiFeature)` in your `tegral { }` block.\n" +
                "-> Building a Ktor application? Use `install(TegralOpenApiKtor)` before creating your routes.\n" +
                "-> If you are just running unit tests, you probably do not care about having OpenAPI descriptions " +
                "available, in which case you can ignore this message."
        )
    }
}

/**
 * Specify additional information about the API via the `RootDsl`.
 *
 * You can use this function to further customize the OpenAPI document (such as the document's title, version,
 * description, etc.). Adding information in an application `describe` block is similar to adding information in the
 * configuration block of [TegralOpenApiKtor].
 *
 * You must install the [TegralOpenApiKtor] plugin before running this function.
 */
fun Application.describe(description: RootDsl.() -> Unit) {
    val openApi = pluginOrNull(TegralOpenApiKtor)
    if (openApi != null) openApi.withRootBuilder(description)
    else printNotInstalledWarning(this)
}

/**
 * Adds an OpenAPI operation description to this route.
 *
 * The [TegralOpenApiKtor] plugin needs to be installed for this to work.
 */
@TegralDsl
@KtorDsl
infix fun Route.describe(description: OperationDsl.() -> Unit): Route {
    val openApi = application.pluginOrNull(TegralOpenApiKtor)
    if (openApi == null) {
        printNotInstalledWarning(application)
        return this
    }

    var metadata = parseMetadataFromRoute(this)

    // Workaround for https://github.com/utybo/Koa/issues/5 | KTOR-4239
    if (metadata.httpMethod == null && this.children.lastOrNull()?.selector is HttpMethodRouteSelector) {
        metadata = metadata.copy(
            httpMethod = (this.children.last().selector as HttpMethodRouteSelector).method
        )
    }

    openApi.registerOperation(
        "/" + metadata.httpPath.asReversed().joinToString("/"),
        metadata.httpMethod!!,
        description
    )
    return this
}

internal data class EndpointMetadata(
    val httpMethod: HttpMethod?,
    val httpPath: List<String>
)

internal data class MutableEndpointMetadata(
    var httpMethod: HttpMethod?,
    val httpPath: MutableList<String>
) {
    fun freeze() = EndpointMetadata(httpMethod, httpPath.toList())
}

internal tailrec fun parseMutableMetadataFromSelector(route: Route?, metadata: MutableEndpointMetadata) {
    if (route == null) return

    when (val selector = route.selector) {
        is HttpMethodRouteSelector -> metadata.httpMethod = selector.method
        is PathSegmentConstantRouteSelector -> metadata.httpPath += selector.value
        is PathSegmentParameterRouteSelector -> metadata.httpPath += selector.prefix.orEmpty() + "{${selector.name}}" +
            selector.suffix.orEmpty()
        else -> {
            /* TODO avoid ignoring silently */
        }
    }
    parseMutableMetadataFromSelector(route.parent, metadata)
}

internal fun parseMetadataFromRoute(route: Route): EndpointMetadata {
    return MutableEndpointMetadata(null, LinkedList()).apply { parseMutableMetadataFromSelector(route, this) }.freeze()
}
