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

package guru.zoroark.tegral.prismakt.generator.protocol

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import org.slf4j.LoggerFactory

class JsonRpcProtocol(scope: InjectionScope) {
    private val objectMapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    private val logger = LoggerFactory.getLogger("tegral.prismakt.rpc")

    private val handler: GeneratorProtocolHandler by scope()

    private fun blockUntilRequest(): GeneratorRequest<*>? {
        logger.trace("Waiting for request")
        val input = readlnOrNull() ?: return null.also { logger.debug("Input EOF reached") }
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
