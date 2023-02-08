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

package guru.zoroark.tegral.openapi.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem

/**
 * DSL for the [path item object](https://spec.openapis.org/oas/v3.1.0#path-item-object).
 */
@TegralDsl
interface PathDsl {
    /**
     * A definition of a GET operation on this path.
     */
    @TegralDsl
    fun get(block: OperationDsl.() -> Unit)

    /**
     * A definition of a POST operation on this path.
     */
    @TegralDsl
    fun post(block: OperationDsl.() -> Unit)

    /**
     * A definition of a PUT operation on this path.
     */
    @TegralDsl
    fun put(block: OperationDsl.() -> Unit)

    /**
     * A definition of a DELETE operation on this path.
     */
    @TegralDsl
    fun delete(block: OperationDsl.() -> Unit)

    /**
     * A definition of a PATCH operation on this path.
     */
    @TegralDsl
    fun patch(block: OperationDsl.() -> Unit)

    /**
     * A definition of an OPTIONS operation on this path.
     */
    @TegralDsl
    fun options(block: OperationDsl.() -> Unit)

    /**
     * A definition of a HEAD operation on this path.
     */
    @TegralDsl
    fun head(block: OperationDsl.() -> Unit)
}

/**
 * Builder for [PathDsl]
 */
class PathBuilder(private val context: OpenApiDslContext) :
    PathDsl,
    @Suppress("DEPRECATION")
    Builder<PathItem>,
    Buildable<PathItem> {
    // TODO summary, description, ...

    private var get: Buildable<Operation>? = null
    private var post: Buildable<Operation>? = null
    private var put: Buildable<Operation>? = null
    private var delete: Buildable<Operation>? = null
    private var patch: Buildable<Operation>? = null
    private var options: Buildable<Operation>? = null
    private var head: Buildable<Operation>? = null

    override fun get(block: OperationDsl.() -> Unit) {
        get = OperationBuilder(context).apply(block)
    }

    override fun post(block: OperationDsl.() -> Unit) {
        post = OperationBuilder(context).apply(block)
    }

    override fun put(block: OperationDsl.() -> Unit) {
        put = OperationBuilder(context).apply(block)
    }

    override fun delete(block: OperationDsl.() -> Unit) {
        delete = OperationBuilder(context).apply(block)
    }

    override fun patch(block: OperationDsl.() -> Unit) {
        patch = OperationBuilder(context).apply(block)
    }

    override fun options(block: OperationDsl.() -> Unit) {
        options = OperationBuilder(context).apply(block)
    }

    override fun head(block: OperationDsl.() -> Unit) {
        head = OperationBuilder(context).apply(block)
    }

    override fun build(): PathItem {
        return PathItem().apply {
            get = this@PathBuilder.get?.build()
            post = this@PathBuilder.post?.build()
            put = this@PathBuilder.put?.build()
            delete = this@PathBuilder.delete?.build()
            patch = this@PathBuilder.patch?.build()
            options = this@PathBuilder.options?.build()
            head = this@PathBuilder.head?.build()
        }
    }
}
