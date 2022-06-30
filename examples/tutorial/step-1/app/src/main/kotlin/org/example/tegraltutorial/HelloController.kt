package org.example.tegraltutorial

import guru.zoroark.tegral.web.controllers.KtorController
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

class HelloController : KtorController() {
    override fun Routing.install() {
        get("/") {
            call.respondText("Hello, world!")
        }
    }
}
