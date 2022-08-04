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

data class EndpointMetadata(
    val httpMethod: HttpMethod? = null,
    val httpPath: List<String> = listOf()
)

internal data class MutableEndpointMetadata(
    var httpMethod: HttpMethod? = null,
    val httpPath: MutableList<String> = LinkedList()
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
    return MutableEndpointMetadata().apply { parseMutableMetadataFromSelector(route, this) }.freeze()
}
