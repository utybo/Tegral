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

package guru.zoroark.tegral.web.apptest

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.server.testing.ClientProvider

/**
 * A [ClientProvider] that can provide clients based on an application name.
 *
 * By default, the [client] and [createClient] functions that come from [ClientProvider] (i.e. the ones that do not take
 * application names) will implicitly use the `null` application name.
 */
interface MultiClientProvider : ClientProvider {
    /**
     * Retrieves a HttpClient with default configurations for the [KtorTestApplication] with the given name.
     */
    fun client(appName: String?): HttpClient

    /**
     * Retrieves a HttpClient with default configurations *and* the configurations provided in the given block for the
     * [KtorTestApplication] with the given name.
     */
    fun createClient(appName: String?, block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit): HttpClient

    override val client: HttpClient get() = client(null)
    override fun createClient(block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit): HttpClient =
        createClient(null, block)
}
