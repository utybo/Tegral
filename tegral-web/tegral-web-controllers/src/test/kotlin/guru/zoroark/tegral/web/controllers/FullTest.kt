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

package guru.zoroark.tegral.web.controllers

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.services.services
import guru.zoroark.tegral.di.services.useServices
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.java.Java
import io.ktor.client.request.get
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

private const val FULL_TEST_PORT = 28810
private const val TEST_NAME = "MyApp"

class FullTest {
    data class ResponseData(val endpoint: String, val message: String)

    class FirstController : KtorController(restrictToAppName = TEST_NAME) {
        override fun Routing.install() {
            get("/test/one") {
                call.respond(ResponseData("one", "One!"))
            }
        }
    }

    class SecondController : KtorController(restrictToAppName = TEST_NAME) {
        override fun Routing.install() {
            get("/test/two") {
                call.respond(ResponseData("two", "Two!!"))
            }
        }
    }

    class App(scope: InjectionScope) : KtorApplication(scope, TEST_NAME) {
        override val settings get() = KtorApplicationSettings(Netty, port = FULL_TEST_PORT)

        override fun Application.setup() {
            this.install(ServerContentNegotiation) { jackson() }
        }
    }

    @Test
    fun `Full application test`() {
        val env = tegralDi {
            useServices()
            meta { put(::KtorExtension) }

            put(::App)
            put(::FirstController)
            put(::SecondController)
        }
        runBlocking {
            env.services.startAll(::println)
            val client = HttpClient(Java) {
                install(ClientContentNegotiation) { jackson() }
            }
            val result = client.get("http://localhost:$FULL_TEST_PORT/test/one").body<ResponseData>()
            assertEquals(ResponseData("one", "One!"), result)
            val result2 = client.get("http://localhost:$FULL_TEST_PORT/test/two").body<ResponseData>()
            assertEquals(ResponseData("two", "Two!!"), result2)
            env.services.stopAll(::println)
        }
    }
}
