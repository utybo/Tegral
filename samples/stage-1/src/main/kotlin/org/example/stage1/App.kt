package org.example.stage1

import guru.zoroark.tegral.web.appdsl.*

val appModule = tegralModule {
    put(::Service)
    put(::Controller)
}

fun main() = tegral {
    put(appModule)
}

class Service {
    fun greet() = "Hello World!"
}

class Controller(scope: InjectionScope) : TegralController {
    private val service: Service by scope()

    override fun Routing.install() {
        get("/hello") {
            call.respondText(service.greet())
        }
    }
}
