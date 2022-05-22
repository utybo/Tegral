package org.example.stage1

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.web.appdsl.tegral
import guru.zoroark.tegral.web.controllers.KtorController
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get


val appModule = tegralDiModule {
    put(::Service)
    put(::Controller)
}

fun main() {
    tegral {
        put(appModule)
    }
}

class Service {
    fun greet() = "Hello World!"
}

class Controller(scope: InjectionScope) : KtorController() {
    private val service: Service by scope()

    override fun Routing.install() {
        get("/hello") {
            call.respondText(service.greet())
        }
    }
}
