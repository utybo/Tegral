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
import guru.zoroark.tegral.openapi.dsl.OpenApiDslContext
import guru.zoroark.tegral.openapi.dsl.OperationBuilder
import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.dsl.RootDsl
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.pluginOrNull
import io.ktor.server.routing.*
import io.ktor.utils.io.*
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
 * Retrieve the [TegralOpenApiKtor] plugin instance from this application, or print a warning message once and return
 * null.
 */
fun Application.getOpenApiOrNullWithMessage(): TegralOpenApiKtor? {
    val plugin = pluginOrNull(TegralOpenApiKtor)
    return if (plugin != null) {
        plugin
    } else {
        printNotInstalledWarning(this)
        null
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
    getOpenApiOrNullWithMessage()?.withRootBuilder(description)
}

/**
 * Adds an OpenAPI operation description to this route.
 *
 * The [TegralOpenApiKtor] plugin needs to be installed for this to work.
 */
@TegralDsl
@KtorDsl
infix fun Route.describe(description: OperationDsl.() -> Unit): Route {
    return describeWith { ctx, hooks ->
        OperationBuilder(ctx).apply { hooks.forEach { it() } }.apply(description)
    }
}

/**
 * A lambda for creating OperationBuilders. This is useful if you need to heavily customize the operation creation
 * process.
 *
 * This lambda receives:
 *
 * - A context, from which types can be created (via [OpenApiDslContext.computeAndRegisterSchema]
 * - A list of hooks that **must** be added to the description *before* anything else.
 */
typealias OperationBuilderWithHooks = (OpenApiDslContext, List<EndpointDescriptionHook>) -> OperationBuilder

/**
 * Adds an OpenAPI operation description to this route using a prepared builder.
 *
 * Refer to [OperationBuilderWithHooks] for details on how to construct custom builders. For most cases, you should
 * instead use [describe].
 *
 * The [TegralOpenApiKtor] plugin needs to be installed for this to work.
 */
@TegralDsl
infix fun Route.describeWith(builder: OperationBuilderWithHooks): Route {
    val openApi = application.getOpenApiOrNullWithMessage() ?: return this

    val metadata = parseMetadataFromRoute(this)

    val hooks = openApi.getHooksForRoute(this)
    val opBuilder = builder(openApi.context, hooks)
    openApi.registerOperation(
        "/" + metadata.httpPath.asReversed().joinToString("/"),
        metadata.httpMethod!!,
        opBuilder
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

internal tailrec fun parseMutableMetadataFromSelector(route: RoutingNode?, metadata: MutableEndpointMetadata) {
    if (route == null) return

    when (val selector = route.selector) {
        is HttpMethodRouteSelector -> metadata.httpMethod = selector.method
        is PathSegmentConstantRouteSelector -> metadata.httpPath += selector.value
        is PathSegmentParameterRouteSelector ->
            metadata.httpPath += selector.prefix.orEmpty() + "{${selector.name}}" + selector.suffix.orEmpty()

        else -> {
            logger.debug("Ignoring unknown route selector type ${selector::class}")
        }
    }
    parseMutableMetadataFromSelector(route.parent, metadata)
}

internal fun parseMetadataFromRoute(route: Route): EndpointMetadata {
    if (route !is RoutingNode) {
        error("Route $route cannot be used with Tegral OpenAPI as it does not inherit from Ktor's RoutingNode")
    }
    return MutableEndpointMetadata(null, LinkedList()).apply { parseMutableMetadataFromSelector(route, this) }.freeze()
}
