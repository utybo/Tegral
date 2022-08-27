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

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.openapi.dsl.OperationDsl
import guru.zoroark.tegral.openapi.ktor.describe
import io.ktor.server.application.ApplicationCall
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.head
import io.ktor.server.resources.options
import io.ktor.server.resources.patch
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.Route
import io.ktor.util.pipeline.PipelineContext
import kotlin.reflect.full.companionObjectInstance

/**
 * Provides endpoint descriptions for [resource classes](https://ktor.io/docs/type-safe-routing.html#resource_classes).
 *
 * This interface should be implemented on a companion object in a resource class. You can either:
 *
 * - Manually implement this interface and its [property][openApi].
 * - Implement by delegating to the [describeResource] function.
 *
 * In order for the description to be taken into account, you must use the `<operation>D` functions (e.g. [getD])
 * instead of the regular [Resources operation functions](https://ktor.io/docs/type-safe-routing.html#define_route)
 * (e.g. `get`).
 */
interface ResourceDescription {
    /**
     * The description for the endpoint represented by this resource class.
     */
    val openApi: OperationDsl.() -> Unit
}

/**
 * Function that should be used to implement [ResourceDescription] on
 * [resource classes](https://ktor.io/docs/type-safe-routing.html#resource_classes).
 *
 * For example, you can do the following:
 *
 * ```
 * @Serializable
 * @Resource("/foo/{bar}")
 * class MyResourceClass(val bar: String) {
 *     companion object : ResourceDescription by describeResource({
 *         description = "Baz"
 *     })
 * }
 * ```
 */
@TegralDsl
fun describeResource(description: OperationDsl.() -> Unit): ResourceDescription {
    return object : ResourceDescription {
        override val openApi = description
    }
}

/**
 * Retrieves a [resource description][ResourceDescription] from the given class' companion object.
 */
inline fun <reified T : Any> descriptionFromCompanionObject(): OperationDsl.() -> Unit =
    (T::class.companionObjectInstance as? ResourceDescription)?.openApi
        ?: { }


/**
 * Defines a `get` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.getD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = get(body) describe descriptionFromCompanionObject<T>()

/**
 * Defines an `options` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.optionsD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = options(body) describe descriptionFromCompanionObject<T>()

/**
 * Defines a `head` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.headD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = head(body) describe descriptionFromCompanionObject<T>()

/**
 * Defines a `post` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.postD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = post(body) describe descriptionFromCompanionObject<T>()

/**
 * Defines a `put` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.putD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = put(body) describe descriptionFromCompanionObject<T>()

/**
 * Defines a `delete` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.deleteD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = delete(body) describe descriptionFromCompanionObject<T>()

/**
 * Defines a `patch` route handler for the given resource, additionally registering its
 * [description][ResourceDescription].
 */
inline fun <reified T : Any> Route.patchD(
    noinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route = patch(body) describe descriptionFromCompanionObject<T>()
