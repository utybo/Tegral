package guru.zoroark.tegral.prismakt.generator

import ch.qos.logback.classic.Level
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.utils.logtools.applyMinimalistLoggingOverrides
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("tegral.prismakt")

interface GeneratorProtocolHandler {
    fun getManifest(request: GeneratorRequest.GetManifestRequest): GeneratorResponse.GetManifestResponse

    fun handle(request: GeneratorRequest<*>): GeneratorResponse<*>? {
        return when (request) {
            is GeneratorRequest.GetManifestRequest -> getManifest(request)
            is GeneratorRequest.GenerateRequest -> {
                generate(request)
                null
            }
        }
    }

    fun generate(request: GeneratorRequest.GenerateRequest)
}

class GeneratorProtocolController : GeneratorProtocolHandler {
    override fun getManifest(request: GeneratorRequest.GetManifestRequest): GeneratorResponse.GetManifestResponse {
        return GeneratorResponse.GetManifestResponse(
            id = request.id,
            jsonrpc = "2.0",
            result = GeneratorResponse.GetManifestResponse.GeneratorManifestContainer(
                GeneratorManifest(
                    prettyName = "Tegral PrismaKt Generator",
                    defaultOutput = "generatedSrc"
                )
            )
        )
    }

    override fun generate(request: GeneratorRequest.GenerateRequest) {
        logger.info(
            "Will generate bindings for the following table(s): " +
                request.params.dmmf.datamodel.models.joinToString { it.name })
    }
}

class JsonRpcProtocol(scope: InjectionScope) {
    private val objectMapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger("tegral.prismakt.rpc")

    private val handler: GeneratorProtocolHandler by scope()

    private fun blockUntilRequest(): GeneratorRequest<*>? {
        logger.trace("Waiting for request")
        val input = readlnOrNull() ?: return null.also { logger.trace("Input EOF reached") }
        logger.trace("Received input: $input")
        val req = runCatching { objectMapper.readValue<GeneratorRequest<*>>(input) }
            .onFailure { logger.error("Failed to parse request", it) }
            .getOrThrow()
        logger.trace("Parsed request: $req")
        return req
    }

    fun exchange() {
        repeat(2) {
            val request = blockUntilRequest() ?: return@repeat
            logger.trace("Calling handler with request ${request.id}")
            val response = handler.handle(request)
            if (response != null) {
                logger.trace("Will send response object: $response")
                val responseStr = objectMapper.writeValueAsString(response)
                logger.trace("Sending response JSON: $responseStr")
                System.err.println(responseStr)
            }
        }
    }
}

fun main() {
    applyMinimalistLoggingOverrides(Level.TRACE)
    logger.warn("Tegral PrismaKt Generator is EXPERIMENTAL! Use at your own risk!")
    val env = tegralDi {
        put(::JsonRpcProtocol)
        put<GeneratorProtocolHandler>(::GeneratorProtocolController)
    }
    val protocol = env.get<JsonRpcProtocol>()
    protocol.exchange()
}