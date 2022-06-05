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
import io.ktor.client.engine.java.Java
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val MULTI_APP_TEST_PORT_1 = 28821
private const val MULTI_APP_TEST_PORT_2 = 28822
private const val MULTI_APP_TEST_PORT_3 = 28823

class ControllerOne : KtorController(restrictToAppName = "one") {
    override fun Routing.install() {
        get("/one") {
            call.respondText("one")
        }
    }
}

class AppOne(scope: InjectionScope) : KtorApplication(scope, "one") {
    override val settings get() = KtorApplicationSettings(Netty, MULTI_APP_TEST_PORT_1)

    override fun Application.setup() {
        // no-op
    }
}

class ControllerTwo : KtorController(restrictToAppName = "two") {
    override fun Routing.install() {
        get("/two") {
            call.respondText("two")
        }
    }
}

class AppTwo(scope: InjectionScope) : KtorApplication(scope, "two") {
    override val settings get() = KtorApplicationSettings(Netty, MULTI_APP_TEST_PORT_2)

    override fun Application.setup() {
        // no-op
    }
}

class ControllerThree : KtorController(restrictToAppName = "three") {
    override fun Routing.install() {
        get("/three") {
            call.respondText("three")
        }
    }
}

class AppThree(scope: InjectionScope) : KtorApplication(scope, "three") {
    override val settings get() = KtorApplicationSettings(Netty, MULTI_APP_TEST_PORT_3)

    override fun Application.setup() {
        // no-op
    }
}

class MultiAppTest {
    @Test
    fun `Multi app test`() {
        val env = tegralDi {
            useServices()
            meta { put(::KtorExtension) }

            put(::AppOne)
            put(::AppTwo)
            put(::AppThree)

            put(::ControllerOne)
            put(::ControllerTwo)
            put(::ControllerThree)
        }
        runBlocking {
            env.services.startAll()

            val client = HttpClient(Java) { expectSuccess = true }

            assertEquals("one", client.get("http://localhost:$MULTI_APP_TEST_PORT_1/one").bodyAsText())
            assertEquals("two", client.get("http://localhost:$MULTI_APP_TEST_PORT_2/two").bodyAsText())
            assertEquals("three", client.get("http://localhost:$MULTI_APP_TEST_PORT_3/three").bodyAsText())

            val endpointToPortsWhichShouldNotFound = mapOf(
                "one" to listOf(MULTI_APP_TEST_PORT_2, MULTI_APP_TEST_PORT_3),
                "two" to listOf(MULTI_APP_TEST_PORT_1, MULTI_APP_TEST_PORT_3),
                "three" to listOf(MULTI_APP_TEST_PORT_1, MULTI_APP_TEST_PORT_2)
            )
            for ((endpoint, ports) in endpointToPortsWhichShouldNotFound) {
                for (port in ports) {
                    assertNotFound { client.get("http://localhost:$port/$endpoint") }
                }
            }

            env.services.stopAll()
        }
    }

    private inline fun assertNotFound(block: () -> Unit) {
        val ex = assertFailsWith<ClientRequestException> {
            block()
        }
        assertEquals(ex.response.status, HttpStatusCode.NotFound)
    }
}
