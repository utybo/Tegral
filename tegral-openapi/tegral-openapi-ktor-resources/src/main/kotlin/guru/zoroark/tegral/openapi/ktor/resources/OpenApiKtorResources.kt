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

@file:Suppress("unused")
package guru.zoroark.tegral.openapi.ktor.resources

import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.ktor.describe
import io.ktor.server.application.*
import io.ktor.server.routing.Route
import io.ktor.server.resources.*
import io.ktor.util.pipeline.*
import kotlin.reflect.full.companionObjectInstance

interface OpenApiDescription {
    val openApi: OperationDsl.() -> Unit
}

inline fun <reified T : Any> description(): OperationDsl.() -> Unit =
    (T::class.companionObjectInstance as? OpenApiDescription)?.openApi
        ?: { }

inline fun <reified T : Any> Route.getD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = get(body) describe description<T>()

inline fun <reified T : Any> Route.optionsD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = options(body) describe description<T>()

inline fun <reified T : Any> Route.headD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = head(body) describe description<T>()

inline fun <reified T : Any> Route.postD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = post(body) describe description<T>()

inline fun <reified T : Any> Route.putD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = put(body) describe description<T>()

inline fun <reified T : Any> Route.deleteD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = delete(body) describe description<T>()

inline fun <reified T : Any> Route.patchD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = patch(body) describe description<T>()