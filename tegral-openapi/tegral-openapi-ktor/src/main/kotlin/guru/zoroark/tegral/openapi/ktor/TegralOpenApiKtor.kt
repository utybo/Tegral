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

import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.dsl.PathDsl
import guru.zoroark.tegral.openapi.dsl.PathsDsl
import guru.zoroark.tegral.openapi.dsl.RootBuilder
import guru.zoroark.tegral.openapi.dsl.RootDsl
import guru.zoroark.tegral.openapi.dsl.SimpleDslContext
import io.ktor.http.HttpMethod
import io.ktor.server.application.plugin
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.BaseApplicationPlugin
import io.ktor.util.AttributeKey
import io.swagger.v3.oas.models.OpenAPI
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TegralOpenApiKtor {
    private val context = SimpleDslContext()
    private val builder: RootBuilder = RootBuilder(context)
    private val logger: Logger = LoggerFactory.getLogger("tegral.openapi.ktor.plugin")

    inner class Configuration : RootDsl by builder

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

    fun buildOpenApiDocument(): OpenAPI {
        val document = builder.build()
        context.persistTo(document)
        return document
    }

    fun registerOperation(path: String, method: HttpMethod, operation: OperationDsl.() -> Unit) {
        builder.apply {
            operation(path, method, operation)
        }
    }

    private fun PathsDsl.operation(path: String, method: HttpMethod, operation: OperationDsl.() -> Unit) {
        when(method) {
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
}

val Application.openApi: TegralOpenApiKtor get() = plugin(TegralOpenApiKtor)
