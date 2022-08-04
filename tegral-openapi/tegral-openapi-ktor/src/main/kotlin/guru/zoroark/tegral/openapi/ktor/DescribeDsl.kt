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

import guru.zoroark.tegral.openapi.dsl.KoaDsl
import guru.zoroark.tegral.openapi.dsl.OperationDsl
import io.ktor.http.HttpMethod
import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.PathSegmentConstantRouteSelector
import io.ktor.server.routing.PathSegmentParameterRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.application
import io.ktor.util.KtorDsl
import java.util.*

@KoaDsl
@KtorDsl
infix fun Route.describe(description: OperationDsl.() -> Unit): Route {
    var metadata = parseMetadataFromRoute(this)

    // Workaround for https://github.com/utybo/Koa/issues/5 | KTOR-4239
    if (metadata.httpMethod == null && this.children.lastOrNull()?.selector is HttpMethodRouteSelector) {
        metadata = metadata.copy(
            httpMethod = (this.children.last().selector as HttpMethodRouteSelector).method
        )
    }

    application.openApi.registerOperation(
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
