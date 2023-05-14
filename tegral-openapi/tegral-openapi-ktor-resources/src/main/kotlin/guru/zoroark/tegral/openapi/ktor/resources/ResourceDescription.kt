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
import guru.zoroark.tegral.openapi.dsl.OperationBuilder
import guru.zoroark.tegral.openapi.dsl.PathBuilder
import guru.zoroark.tegral.openapi.dsl.PathDsl
import guru.zoroark.tegral.openapi.ktor.OperationBuilderWithHooks
import guru.zoroark.tegral.openapi.ktor.PathDescriptionHook
import io.ktor.resources.Resource
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation

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
     * The description for the endpoints represented by this resource class.
     */
    val openApi: PathDsl.() -> Unit
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
fun describeResource(description: PathDsl.() -> Unit): ResourceDescription {
    return object : ResourceDescription {
        override val openApi = description
    }
}

private val logger = LoggerFactory.getLogger("tegral.openapi.ktor.resources")

private object EmptyDescription : ResourceDescription by describeResource({ })

private fun KClass<*>.findResourceDescription(): ResourceDescription {
    val rd = companionObjectInstance as? ResourceDescription
    if (rd != null) {
        return rd
    } else {
        logger.warn(
            "Tried to retrieve the resource description of $this, but was not found because " +
                when (companionObjectInstance) {
                    null ->
                        "the class does not have a companion object"

                    is ResourceDescription ->
                        "the class' companion object does not implement the ResourceDescription interface."

                    else -> "of an unknown reason. Please report this."
                }
        )
        return EmptyDescription
    }
}

private tailrec fun KClass<*>.findParentDescriptions(
    acc: MutableList<ResourceDescription> = mutableListOf()
): List<ResourceDescription> {
    val parent = this.declaredMemberProperties
        .mapNotNull { it.returnType.classifier as? KClass<*> }
        .firstOrNull { it.hasAnnotation<Resource>() }
        ?: return acc
    acc.add(parent.findResourceDescription())
    return parent.findParentDescriptions(acc)
}

private fun PathDsl.clearOperations() {
    get = null
    post = null
    put = null
    delete = null
    patch = null
    options = null
    head = null
}

private fun PathDsl.inferNullOperations() {
    if (get == null) get { }
    if (post == null) post { }
    if (put == null) put { }
    if (delete == null) delete { }
    if (patch == null) patch { }
    if (options == null) options { }
    if (head == null) head { }
}

/**
 * This type is intended to be used in a lambda calling one of the HTTP method properties of [PathDsl], such as
 * [PathDsl.get], [PathDsl.post], etc.
 */
typealias OperationSelector = PathDsl.() -> OperationBuilder?

/**
 * Create a description from the information contained in the resource [T].
 */
fun <T : Any> descriptionFromResource(
    resourceClass: KClass<T>,
    operationSelector: OperationSelector
): OperationBuilderWithHooks {
    return { ctx, hooks ->
        val obj = resourceClass.findResourceDescription()
        val parentHooks: List<PathDescriptionHook> = resourceClass
            .findParentDescriptions()
            .map { desc -> { desc.openApi.invoke(this); this.clearOperations() } }
        val builder = PathBuilder(ctx).apply {
            hooks.forEach { it() }
            parentHooks.asReversed().forEach { it() }
            obj.openApi.invoke(this)
            inferNullOperations()
        }
        builder.operationSelector()!!
    }
}

/**
 * Create a description from the information contained in the resource [T].
 */
@TegralDsl
inline fun <reified T : Any> descriptionFromResource(
    noinline operationSelector: OperationSelector
): OperationBuilderWithHooks =
    descriptionFromResource(T::class, operationSelector)
