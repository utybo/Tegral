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
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.security.SecurityRequirement

/**
 * DSL for the [path item object](https://spec.openapis.org/oas/v3.1.0#path-item-object).
 *
 * Any operation-related data entered at the path level are applied by default to all the paths defined *after* the data
 * is specified.
 */
@TegralDsl
interface PathDsl : OperationDsl {
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

private const val WriteOnlyErrorMessage =
    "Operation functions, when used on a path instead of an actual operation, are write-only"

/**
 * Builder for [PathDsl]
 */
class PathBuilder(
    private val context: OpenApiDslContext
) :
    PathDsl,
    @Suppress("DEPRECATION")
    Builder<PathItem>,
    Buildable<PathItem> {

    private var get: Buildable<Operation>? = null
    private var post: Buildable<Operation>? = null
    private var put: Buildable<Operation>? = null
    private var delete: Buildable<Operation>? = null
    private var patch: Buildable<Operation>? = null
    private var options: Buildable<Operation>? = null
    private var head: Buildable<Operation>? = null

    private val toApplyToOperations = mutableListOf<OperationDsl.() -> Unit>()
    private fun addOperationDefault(block: OperationDsl.() -> Unit) {
        toApplyToOperations += block
    }
    private fun newOperation(): OperationBuilder {
        val op = OperationBuilder(context)
        toApplyToOperations.forEach { it(op) }
        return op
    }

    override fun get(block: OperationDsl.() -> Unit) {
        get = newOperation().apply(block)
    }

    override fun post(block: OperationDsl.() -> Unit) {
        post = newOperation().apply(block)
    }

    override fun put(block: OperationDsl.() -> Unit) {
        put = newOperation().apply(block)
    }

    override fun delete(block: OperationDsl.() -> Unit) {
        delete = newOperation().apply(block)
    }

    override fun patch(block: OperationDsl.() -> Unit) {
        patch = newOperation().apply(block)
    }

    override fun options(block: OperationDsl.() -> Unit) {
        options = newOperation().apply(block)
    }

    override fun head(block: OperationDsl.() -> Unit) {
        head = newOperation().apply(block)
    }

    override var summary: String?
        get() = error(WriteOnlyErrorMessage)
        set(value) {
            addOperationDefault { summary = value }
        }
    override var description: String?
        get() = error(WriteOnlyErrorMessage)
        set(value) {
            addOperationDefault { description = value }
        }
    override var externalDocsDescription: String?
        get() = error(WriteOnlyErrorMessage)
        set(value) {
            addOperationDefault { externalDocsDescription = value }
        }
    override var externalDocsUrl: String?
        get() = error(WriteOnlyErrorMessage)
        set(value) {
            addOperationDefault { externalDocsUrl = value }
        }
    override var requestBody: RequestBodyBuilder?
        get() = error(WriteOnlyErrorMessage)
        set(_) {
            error("requestBody is not compatible with PathDsl, use the body function instead")
        }
    override var deprecated: Boolean?
        get() = error(WriteOnlyErrorMessage)
        set(value) {
            addOperationDefault { deprecated = value }
        }
    override var operationId: String?
        get() = error(WriteOnlyErrorMessage)
        set(value) {
            addOperationDefault { operationId = value }
        }
    override val parameters: MutableList<Buildable<Parameter>>
        get() = error(WriteOnlyErrorMessage)
    override val securityRequirements: MutableList<SecurityRequirement>
        get() = error(WriteOnlyErrorMessage)
    override val responses: MutableMap<Int, Buildable<ApiResponse>>
        get() = error(WriteOnlyErrorMessage)
    override val tags: MutableList<String>
        get() = error(WriteOnlyErrorMessage)

    override fun security(key: String) {
        addOperationDefault { security(key) }
    }

    override fun security(key: String, vararg scopes: String) {
        addOperationDefault { security(key, *scopes) }
    }

    override fun Int.response(builder: ResponseDsl.() -> Unit) {
        addOperationDefault { this@response.response(builder)   }
    }

    override fun String.pathParameter(builder: ParameterDsl.() -> Unit) {
        addOperationDefault { this@pathParameter.pathParameter(builder) }
    }

    override fun String.headerParameter(builder: ParameterDsl.() -> Unit) {
        addOperationDefault { this@headerParameter.headerParameter(builder) }
    }

    override fun String.cookieParameter(builder: ParameterDsl.() -> Unit) {
        addOperationDefault { this@cookieParameter.cookieParameter(builder) }
    }

    override fun String.queryParameter(builder: ParameterDsl.() -> Unit) {
        addOperationDefault { this@queryParameter.queryParameter(builder) }
    }

    override fun body(builder: RequestBodyDsl.() -> Unit) {
        addOperationDefault { body(builder) }
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
