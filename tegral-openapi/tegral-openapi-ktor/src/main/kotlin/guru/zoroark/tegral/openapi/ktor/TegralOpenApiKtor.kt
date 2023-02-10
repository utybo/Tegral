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

import guru.zoroark.tegral.openapi.dsl.OperationBuilder
import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.dsl.PathDsl
import guru.zoroark.tegral.openapi.dsl.PathsDsl
import guru.zoroark.tegral.openapi.dsl.RootBuilder
import guru.zoroark.tegral.openapi.dsl.RootDsl
import guru.zoroark.tegral.openapi.dsl.SimpleDslContext
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.server.application.plugin
import io.ktor.server.routing.Route
import io.ktor.util.AttributeKey
import io.swagger.v3.oas.models.OpenAPI
import org.slf4j.Logger
import org.slf4j.LoggerFactory

typealias EndpointDescriptionHook = OperationDsl.() -> Unit
typealias PathDescriptionHook = PathDsl.() -> Unit

/**
 * A Ktor plugin that adds a Tegral OpenAPI DSL integration to the Ktor application, including:
 *
 * - Support for providing basic OpenAPI information right from the `install(...) { ... }` call.
 * - Support for [describing][describe] endpoints and [registering][registerOperation] operations.
 * - Producing an OpenAPI document [as an object][buildOpenApiDocument]
 * - Support for serving OpenAPI documents from an endpoint
 */
class TegralOpenApiKtor {
    /**
     * The context used by this plugin. This can be used to register new types.
     */
    val context = SimpleDslContext()
    private val builder: RootBuilder = RootBuilder(context)
    private val logger: Logger = LoggerFactory.getLogger("tegral.openapi.ktor.plugin")
    private val hooks: MutableMap<Route, MutableList<OperationDsl.() -> Unit>> = mutableMapOf()

    /**
     * Configuration block for the plugin, allows you to directly add information to the OpenAPI document.
     */
    inner class Configuration : RootDsl by builder

    /**
     * Ktor plugin object for [TegralOpenApiKtor].
     */
    companion object Plugin : BaseApplicationPlugin<ApplicationCallPipeline, Configuration, TegralOpenApiKtor> {
        override val key = AttributeKey<TegralOpenApiKtor>("TegralOpenApiKtor")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): TegralOpenApiKtor {
            val feature = TegralOpenApiKtor()
            feature.Configuration().apply(configure)
            return feature
        }
    }

    /**
     * Create an OpenAPI document from the information registered in this plugin.
     */
    fun buildOpenApiDocument(): OpenAPI {
        val document = builder.build()
        context.persistTo(document)
        return document
    }

    /**
     * Register an OpenAPI operation from a path, HTTP method and operation builder.
     *
     * This function will *not* apply hooks, you should apply hooks yourself by calling [getHooksForRoute] and applying
     * them.
     *
     * You should use [describe] instead of this function.
     */
    fun registerOperation(path: String, method: HttpMethod, operation: OperationDsl.() -> Unit) {
        builder.apply {
            operation(path, method, operation)
        }
    }

    fun registerOperation(path: String, method: HttpMethod, operationBuilder: OperationBuilder) {
        builder.apply {
            operation(path, method, operationBuilder)
        }
    }

    private fun PathsDsl.operation(path: String, method: HttpMethod, operation: OperationDsl.() -> Unit) {
        when (method) {
            HttpMethod.Get -> path get operation
            HttpMethod.Post -> path post operation
            HttpMethod.Put -> path put operation
            HttpMethod.Delete -> path delete operation
            HttpMethod.Patch -> path patch operation
            HttpMethod.Head -> path head operation
            HttpMethod.Options -> path options operation
            else -> logger.warn("Ignoring unsupported HTTP method $method (while registering '$path')")
        }
    }

    private fun PathsDsl.operation(path: String, method: HttpMethod, operationBuilder: OperationBuilder) {
        when (method) {
            HttpMethod.Get -> path { get = operationBuilder }
            HttpMethod.Post -> path { post = operationBuilder }
            HttpMethod.Put -> path { put = operationBuilder }
            HttpMethod.Delete -> path { delete = operationBuilder }
            HttpMethod.Patch -> path { patch = operationBuilder }
            HttpMethod.Head -> path { head = operationBuilder }
            HttpMethod.Options -> path { options = operationBuilder }
            else -> logger.warn("Ignoring unsupported HTTP method $method (while registering '$path')")
        }
    }

    /**
     * Runs the given lambda against the root OpenAPI builder, allowing to further customize the content of the OpenAPI
     * document.
     *
     * See [Application.describe] for more information.
     */
    fun withRootBuilder(description: RootDsl.() -> Unit) {
        builder.apply(description)
    }

    /**
     * Register a hook for the given route's children.
     */
    fun registerSubrouteHook(route: Route, hook: OperationDsl.() -> Unit) {
        hooks.compute(route) { _, existing ->
            if (existing == null) {
                mutableListOf(hook)
            } else {
                existing.add(hook)
                existing
            }
        }
    }

    private fun Route.parentsExcludingSelf(): Sequence<Route> {
        val result = mutableListOf<Route>()
        var current: Route? = this.parent
        while (current != null) {
            result.add(current)
            current = current.parent
        }
        return result.asReversed().asSequence()
    }

    /**
     * Retrieve all registered hooks for the parents of the given route.
     */
    fun getHooksForRoute(route: Route): List<EndpointDescriptionHook> =
        route.parentsExcludingSelf().flatMap { hooks[it]?.asSequence() ?: emptySequence() }.toList()
}

/**
 * Retrieve the [TegralOpenApiKtor] plugin from this application.
 */
val Application.openApi: TegralOpenApiKtor get() = plugin(TegralOpenApiKtor)
