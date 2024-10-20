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

@file:Suppress("MatchingDeclarationName")

package guru.zoroark.tegral.openapi.ktor.resources

import guru.zoroark.tegral.openapi.ktor.describeWith
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.head
import io.ktor.server.resources.options
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext

/**
 * Defines a `get` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.getD(
    noinline body: suspend RoutingContext.(T) -> Unit
): Route = get(body) describeWith descriptionFromResource<T> { get }

/**
 * Defines an `options` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.optionsD(
    noinline body: suspend RoutingContext.(T) -> Unit
): Route = options(body) describeWith descriptionFromResource<T> { options }

/**
 * Defines a `head` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.headD(
    noinline body: suspend RoutingContext.(T) -> Unit
): Route = head(body) describeWith descriptionFromResource<T> { head }

/**
 * Defines a `post` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.postD(
    noinline body: suspend RoutingContext.(T) -> Unit
): Route = post(body) describeWith descriptionFromResource<T> { post }

/**
 * Defines a `put` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.putD(
    noinline body: suspend RoutingContext.(T) -> Unit
): Route = put(body) describeWith descriptionFromResource<T> { put }

/**
 * Defines a `delete` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.deleteD(
    noinline body: suspend RoutingContext.(T) -> Unit
): Route = delete(body) describeWith descriptionFromResource<T> { delete }

/**
 * Defines a `patch` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.patchD(
    noinline body: suspend RoutingContext.(T) -> Unit
): Route = patch(body) describeWith descriptionFromResource<T> { patch }
