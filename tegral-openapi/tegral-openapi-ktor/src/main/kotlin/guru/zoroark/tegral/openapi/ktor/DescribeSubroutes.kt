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
import io.ktor.server.routing.Route
import io.ktor.server.routing.application

/**
 * Adds a hook to the given route. All following descriptions on endpoints that are sub-endpoints of this route will
 * have the given hook applied to their description.
 */
@TegralDsl
fun Route.describeSubroutes(hook: OperationDsl.() -> Unit) {
    val openApi = application.getOpenApiOrNullWithMessage() ?: return
    openApi.registerSubrouteHook(this, hook)
}
