package guru.zoroark.tegral.e2e.noconfig

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.web.appdsl.tegral
import kotlin.test.Test
import guru.zoroark.tegral.web.controllers.KtorController
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

class Controller : KtorController() {
    override fun Routing.install() {
        get("/") {
            call.respondText("Hello!")
        }
    }

}

class TestLaunch {
    @Test
    fun `Test launch without config`() {
        val tegral = tegral {
            put(::Controller)
        }
        try {
            HttpClient(Java).use {
                val res = runBlocking {
                    it.get("http://localhost:8080/").bodyAsText()
                }
                assertEquals("Hello!", res)
            }
        } finally {
            runBlocking { tegral.stop() }
        }
    }
}
