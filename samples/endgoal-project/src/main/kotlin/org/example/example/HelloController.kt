package org.example.endgoal

class HelloController(scope: InjectionScope) : TegralController() {
    override fun Route.install() {
        get("/hello") {
            call.respondText("Hello World!")
        }
    }
}
