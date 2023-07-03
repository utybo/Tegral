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

package guru.zoroark.tegral.web.controllers.fundef

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.extensions.fundef.ExperimentalFundef
import guru.zoroark.tegral.di.extensions.fundef.Fundef
import guru.zoroark.tegral.di.services.services
import guru.zoroark.tegral.di.services.useServices
import guru.zoroark.tegral.web.controllers.KtorApplication
import guru.zoroark.tegral.web.controllers.KtorApplicationSettings
import guru.zoroark.tegral.web.controllers.KtorController
import guru.zoroark.tegral.web.controllers.KtorExtension
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.call
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class ControllerA(scope: InjectionScope) : KtorController() {
    private val responseService: ResponseService by scope()

    override fun Routing.install() {
        get("/a") {
            call.respondText(responseService.createTextFor("A"))
        }
    }
}

@OptIn(ExperimentalFundef::class)
@Fundef
fun Routing.controllerB(responseService: ResponseService) {
    get("/b") {
        call.respondText(responseService.createTextFor("B"))
    }
}

class ResponseService {
    fun createTextFor(controllerId: String) = "Controller $controllerId"
}

private const val TEST_PORT = 27272

class App(scope: InjectionScope) : KtorApplication(scope) {
    override val settings = KtorApplicationSettings(Netty, port = TEST_PORT)
}

class FundefControllersTest {
    @Test
    fun `Test with fundefs`() {
        val env = tegralDi {
            useServices()
            meta { put { KtorExtension(scope, true) } }

            put(::ResponseService)
            put(::App)
            put(::ControllerA)
            put(Routing::controllerB)
        }
        runBlocking {
            env.services.startAll()

            val client = HttpClient(Java) { expectSuccess = true }
            assertEquals("Controller A", client.get("http://localhost:$TEST_PORT/a").bodyAsText())
            assertEquals("Controller B", client.get("http://localhost:$TEST_PORT/b").bodyAsText())
        }
    }
}
