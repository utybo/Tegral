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

package guru.zoroark.tegral.openapi.feature

import guru.zoroark.tegral.openapi.ktor.openApiEndpoint
import guru.zoroark.tegral.openapi.ktorui.swaggerUiEndpoint
import guru.zoroark.tegral.web.controllers.KtorController
import io.ktor.server.routing.Routing

/**
 * Sets up the following endpoints:
 *
 * - `/_t/openapi` for the OpenAPI document
 * - `/_t/swagger` (and its subroutes) for Swagger UI
 *
 * This class is part of the [OpenApiFeature].
 */
class OpenApiEndpoints : KtorController() {
    override fun Routing.install() {
        openApiEndpoint("/_t/openapi")
        swaggerUiEndpoint("/_t/swagger", "/_t/openapi")
    }
}
