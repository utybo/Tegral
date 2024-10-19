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

package guru.zoroark.tegral.openapi.ktor.resources

import guru.zoroark.tegral.openapi.ktor.TegralOpenApiKtor
import guru.zoroark.tegral.openapi.ktor.openApi
import io.ktor.resources.Resource
import io.ktor.server.application.ApplicationCall
import io.ktor.server.resources.Resources
import io.ktor.server.routing.*
import io.ktor.server.testing.testApplication
import io.ktor.util.pipeline.PipelineContext
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Resource("/hello/{name}")
@Serializable
class Hello(val name: String) {
    companion object : ResourceDescription by describeResource({
        description = "Returns a greeting"
    })
}

typealias RouteBuilder = Route.(suspend RoutingContext.(Hello) -> Unit) -> Route

class OperationDRouteHandlersTest {
    private fun testResourceOperation(
        routeBuilder: RouteBuilder,
        operationGetter: PathItem.() -> Operation
    ) {
        testApplication {
            install(TegralOpenApiKtor)
            install(Resources)

            routing {
                routeBuilder {
                    // no-op
                }
            }

            application {
                val openApi = openApi.buildOpenApiDocument()
                val operation = openApi.paths["/hello/{name}"]?.operationGetter()
                assertNotNull(operation)
                assertEquals("Returns a greeting", operation.description)
            }
        }
    }

    @Test
    fun `Get resource with description`() {
        testResourceOperation(
            Route::getD,
            PathItem::getGet
        )
    }

    @Test
    fun `Post resource with description`() {
        testResourceOperation(
            Route::postD,
            PathItem::getPost
        )
    }

    @Test
    fun `Put resource with description`() {
        testResourceOperation(
            Route::putD,
            PathItem::getPut
        )
    }

    @Test
    fun `Patch resource with description`() {
        testResourceOperation(
            Route::patchD,
            PathItem::getPatch
        )
    }

    @Test
    fun `Options resource with description`() {
        testResourceOperation(
            Route::optionsD,
            PathItem::getOptions
        )
    }

    @Test
    fun `Delete resource with description`() {
        testResourceOperation(
            Route::deleteD,
            PathItem::getDelete
        )
    }

    @Test
    fun `Head resource with description`() {
        testResourceOperation(
            Route::headD,
            PathItem::getHead
        )
    }
}
