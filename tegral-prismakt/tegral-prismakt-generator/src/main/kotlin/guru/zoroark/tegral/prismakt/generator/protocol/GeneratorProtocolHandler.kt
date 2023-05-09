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

/**
 * A simple interface of the Prisma Generator JSON RPC protocol.
 *
 * See [here](https://prismaio.notion.site/Prisma-Generators-a2cdf262207a4e9dbcd0e362dfac8dc0) for more information.
 */
interface GeneratorProtocolHandler {
    /**
     * Corresponds to the 'getManifest' JSON RPC method.
     */
    fun getManifest(request: GeneratorRequest.GetManifestRequest): GeneratorResponse.GetManifestResponse

    /**
     * Corresponds to the 'generate' JSON RPC method.
     */
    fun generate(request: GeneratorRequest.GenerateRequest)

    /**
     * Dispatches the provided request to one of the functions in this interface.
     */
    fun handle(request: GeneratorRequest<*>): GeneratorResponse<*>? {
        return when (request) {
            is GeneratorRequest.GetManifestRequest -> getManifest(request)
            is GeneratorRequest.GenerateRequest -> {
                generate(request)
                null
            }
        }
    }
}
