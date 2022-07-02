package org.example.tegraltutorial

import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.web.controllers.KtorController
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

class HelloController(scope: InjectionScope) : KtorController() {
    private val helloService: HelloService by scope()

    override fun Routing.install() {
        get("/") {
            call.respondText(helloService.greet())
        }
    }
}
