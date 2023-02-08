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
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement

/**
 * DSL for the [operation object](https://spec.openapis.org/oas/v3.1.0#operation-object).
 *
 * Note that the `externalDocs` object is embedded in this DSL.
 */
@TegralDsl
interface OperationDsl {
    /**
     * A short summary of what the operation does.
     */
    @TegralDsl
    var summary: String?

    /**
     * A verbose explanation of the operation behavior. CommonMark syntax may be used for rich text representation.
     */
    @TegralDsl
    var description: String?

    /**
     * A description of the additional external documentation for this operation. CommonMark syntax may be used for rich
     * text representation.
     */
    @TegralDsl
    var externalDocsDescription: String?

    /**
     * A URL that points to additional external documentation for this operation. Must be a valid URL.
     */
    @TegralDsl
    var externalDocsUrl: String?

    /**
     * The request body applicable for this operation.
     */
    @TegralDsl
    var requestBody: RequestBodyBuilder?

    /**
     * If true, declares this operation to be deprecated (false by default).
     */
    @TegralDsl
    var deprecated: Boolean?

    /**
     * A string used to identify the operation, unique among all operations described in the API.
     */
    @TegralDsl
    var operationId: String?

    /**
     * Parameters that are applicable for this operation.
     *
     * See the following functions to easily create parameters:
     *
     * - [pathParameter]
     * - [headerParameter]
     * - [cookieParameter]
     * - [queryParameter]
     */
    @TegralDsl
    val parameters: MutableList<Buildable<Parameter>>

    /**
     * A declaration of which security mechanisms can be used for this operation.
     *
     * - This list behaves like an "OR", only one needs to be fulfilled for the operation.
     * - Requirements defined in the individual `SecurityRequirement` objects behave like an "AND", and all of them need
     * to be fulfilled.
     */
    @TegralDsl
    val securityRequirements: MutableList<SecurityRequirement>

    /**
     * The list of possible responses as they are returned from executing this operation.
     */
    @TegralDsl
    val responses: MutableMap<Int, Buildable<ApiResponse>>

    /**
     * A list of tags for API documentation control. Tags can be used for logical grouping of operations by resources or
     * any other qualifier.
     */
    @TegralDsl
    val tags: MutableList<String>

    /**
     * Adds a security requirement object to this operation with the given key.
     */
    @TegralDsl
    fun security(key: String)

    /**
     * Adds a security requirement object to this operation with the given key and scopes.
     */
    @TegralDsl
    fun security(key: String, vararg scopes: String)

    /**
     * Creates a response for the given response code (passed as an integer value).
     */
    @TegralDsl
    infix fun Int.response(builder: ResponseDsl.() -> Unit)

    /**
     * Creates a path parameter, with the given string as the name of the corresponding path segment.
     */
    @TegralDsl
    infix fun String.pathParameter(builder: ParameterDsl.() -> Unit)

    /**
     * Creates a header parameter, with the given string as the name of the header.
     */
    @TegralDsl
    infix fun String.headerParameter(builder: ParameterDsl.() -> Unit)

    /**
     * Creates a cookie parameter, with the given string as the name of the cookie.
     */
    @TegralDsl
    infix fun String.cookieParameter(builder: ParameterDsl.() -> Unit)

    /**
     * Creates a query parameter, with the given string as the name of the query parameter key.
     */
    @TegralDsl
    infix fun String.queryParameter(builder: ParameterDsl.() -> Unit)

    /**
     * Defines the request body for this operation.
     */
    @TegralDsl
    fun body(builder: RequestBodyDsl.() -> Unit)
}

/**
 * Builder for [OperationDsl]
 */
@TegralDsl
@Suppress("TooManyFunctions")
class OperationBuilder(private val context: OpenApiDslContext) : OperationDsl, @Suppress("DEPRECATION") Builder<Operation>, Buildable<Operation> {
    override var summary: String? = null
    override val responses = mutableMapOf<Int, Buildable<ApiResponse>>()
    override var description: String? = null
    override var externalDocsDescription: String? = null
    override var externalDocsUrl: String? = null
    override var requestBody: RequestBodyBuilder? = null
    override var deprecated: Boolean? = null
    override var operationId: String? = null

    // TODO callbacks, servers

    override val tags = mutableListOf<String>()
    override val parameters = mutableListOf<Buildable<Parameter>>()
    override val securityRequirements = mutableListOf<SecurityRequirement>()

    // TODO properly support AND scenarios between security requirements (right now it's OR only)

    override fun security(key: String) {
        securityRequirements.add(SecurityRequirement().addList(key))
    }

    override fun security(key: String, vararg scopes: String) {
        securityRequirements.add(SecurityRequirement().addList(key, scopes.toList()))
    }

    override infix fun Int.response(builder: ResponseDsl.() -> Unit) {
        responses[this] = ResponseBuilder(context).apply(builder)
    }

    override fun body(builder: RequestBodyDsl.() -> Unit) {
        requestBody = RequestBodyBuilder(context).apply(builder)
    }

    override infix fun String.pathParameter(builder: ParameterDsl.() -> Unit) {
        parameters += ParameterBuilder(context, this, ParameterKind.Path).apply(builder)
    }

    override infix fun String.headerParameter(builder: ParameterDsl.() -> Unit) {
        parameters += ParameterBuilder(context, this, ParameterKind.Header).apply(builder)
    }

    override infix fun String.cookieParameter(builder: ParameterDsl.() -> Unit) {
        parameters += ParameterBuilder(context, this, ParameterKind.Cookie).apply(builder)
    }

    override infix fun String.queryParameter(builder: ParameterDsl.() -> Unit) {
        parameters += ParameterBuilder(context, this, ParameterKind.Query).apply(builder)
    }

    override fun build(): Operation = Operation().apply {
        summary = this@OperationBuilder.summary
        if (this@OperationBuilder.responses.isNotEmpty()) {
            responses(
                ApiResponses().apply {
                    for ((returnCode, responseBuilder) in this@OperationBuilder.responses) {
                        addApiResponse(returnCode.toString(), responseBuilder.build())
                    }
                }
            )
        }
        if (this@OperationBuilder.tags.isNotEmpty()) {
            tags = this@OperationBuilder.tags
        }
        description = this@OperationBuilder.description
        if (externalDocsUrl != null || externalDocsDescription != null) {
            externalDocs = ExternalDocumentation().apply {
                description = externalDocsDescription
                url = externalDocsUrl
            }
        }
        deprecated = this@OperationBuilder.deprecated
        requestBody = this@OperationBuilder.requestBody?.build()
        parameters = this@OperationBuilder.parameters.map { it.build() }.ifEmpty { null }
        security = this@OperationBuilder.securityRequirements.ifEmpty { null }
        operationId = this@OperationBuilder.operationId
    }
}
