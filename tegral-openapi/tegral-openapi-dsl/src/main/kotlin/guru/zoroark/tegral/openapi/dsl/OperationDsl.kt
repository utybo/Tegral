package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement

interface OperationDsl {
    var summary: String?
    var description: String?
    var externalDocsDescription: String?
    var externalDocsUrl: String?
    var requestBody: RequestBodyBuilder?
    var deprecated: Boolean?
    var operationId: String?

    val parameters: MutableList<Builder<Parameter>>
    val securityRequirements: MutableList<SecurityRequirement>
    val responses: MutableMap<Int, Builder<ApiResponse>>
    val tags: MutableList<String>

    fun security(key: String)
    fun security(key: String, vararg scopes: String)

    @KoaDsl
    infix fun Int.response(builder: ResponseBuilder.() -> Unit)

    @KoaDsl
    operator fun String.invoke(builder: BodyStub.() -> Unit): BodyStub

    @KoaDsl
    infix fun String.pathParameter(builder: ParameterBuilder.() -> Unit)

    @KoaDsl
    infix fun String.headerParameter(builder: ParameterBuilder.() -> Unit)

    @KoaDsl
    infix fun String.cookieParameter(builder: ParameterBuilder.() -> Unit)

    @KoaDsl
    infix fun String.queryParameter(builder: ParameterBuilder.() -> Unit)

    @KoaDsl
    infix fun String.requestBody(builder: BodyStub.() -> Unit)

    @KoaDsl
    infix fun Int.response(stub: BodyStub)

    fun body(builder: RequestBodyBuilder.() -> Unit)
}

@KoaDsl
@Suppress("TooManyFunctions")
class OperationBuilder(private val context: KoaDslContext) : OperationDsl, Builder<Operation> {
    override var summary: String? = null
    override val responses = mutableMapOf<Int, Builder<ApiResponse>>()
    override var description: String? = null
    override var externalDocsDescription: String? = null
    override var externalDocsUrl: String? = null
    override var requestBody: RequestBodyBuilder? = null
    override var deprecated: Boolean? = null
    override var operationId: String? = null

    // TODO operationId, callbacks, servers

    override val tags = mutableListOf<String>()
    override val parameters = mutableListOf<Builder<Parameter>>()
    override val securityRequirements = mutableListOf<SecurityRequirement>()

    // TODO properly support AND scenarios between security requirements (right now it's OR only)

    override fun security(key: String) {
        securityRequirements.add(SecurityRequirement().addList(key))
    }

    override fun security(key: String, vararg scopes: String) {
        securityRequirements.add(SecurityRequirement().addList(key, scopes.toList()))
    }

    @KoaDsl
    override infix fun Int.response(builder: ResponseBuilder.() -> Unit) {
        responses[this] = ResponseBuilder(context).apply(builder)
    }

    @KoaDsl
    override infix fun Int.response(stub: BodyStub) {
        responses[this] = Builder {
            ResponseBuilder(context).apply {
                // Transfer response information from stub
                description = stub.description
                contentTypes.add(stub.contentType to stub.mediaTypeBuilder)
            }.build()
        }
    }

    @KoaDsl
    override infix fun String.requestBody(builder: BodyStub.() -> Unit) {
        val stub = BodyStub(this, context).apply(builder)
        requestBody = RequestBodyBuilder(context).apply {
            description = stub.description
            contentTypes.add(stub.contentType to stub.mediaTypeBuilder)
        }
    }

    override fun body(builder: RequestBodyBuilder.() -> Unit) {
        requestBody = RequestBodyBuilder(context).apply(builder)
    }

    @KoaDsl
    override operator fun String.invoke(builder: BodyStub.() -> Unit): BodyStub =
        BodyStub(this, context).apply(builder)

    @KoaDsl
    override infix fun String.pathParameter(builder: ParameterBuilder.() -> Unit) {
        parameters += ParameterBuilder(context, this, ParameterKind.Path).apply(builder)
    }

    @KoaDsl
    override infix fun String.headerParameter(builder: ParameterBuilder.() -> Unit) {
        parameters += ParameterBuilder(context, this, ParameterKind.Header).apply(builder)
    }

    @KoaDsl
    override infix fun String.cookieParameter(builder: ParameterBuilder.() -> Unit) {
        parameters += ParameterBuilder(context, this, ParameterKind.Cookie).apply(builder)
    }

    @KoaDsl
    override infix fun String.queryParameter(builder: ParameterBuilder.() -> Unit) {
        parameters += ParameterBuilder(context, this, ParameterKind.Query).apply(builder)
    }

    override fun build(): Operation = Operation().apply {
        summary = this@OperationBuilder.summary
        responses(ApiResponses().apply {
            for ((returnCode, responseBuilder) in this@OperationBuilder.responses) {
                addApiResponse(returnCode.toString(), responseBuilder.build())
            }
        })
        tags = this@OperationBuilder.tags
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
