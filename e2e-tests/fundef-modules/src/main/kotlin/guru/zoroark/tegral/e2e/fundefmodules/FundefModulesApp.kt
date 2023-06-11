@file:OptIn(ExperimentalFundef::class)

package guru.zoroark.tegral.e2e.fundefmodules

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.extensions.fundef.ExperimentalFundef
import guru.zoroark.tegral.di.extensions.fundef.Fundef
import guru.zoroark.tegral.openapi.dsl.schema
import guru.zoroark.tegral.openapi.feature.OpenApiFeature
import guru.zoroark.tegral.openapi.ktor.describe
import guru.zoroark.tegral.web.appdsl.install
import guru.zoroark.tegral.web.appdsl.tegral
import guru.zoroark.tegral.web.controllers.WebControllersFeature
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

class Repository {
    private var stored: String? = null

    fun retrieve() = stored

    fun store(newValue: String): String {
        return newValue.also { stored = newValue }
    }

    fun delete() {
        stored = null
    }
}

class Service(scope: InjectionScope) {
    private val repository: Repository by scope()

    fun getString() = repository.retrieve()

    fun updateString(newString: String): String {
        require(newString.isNotBlank()) { "New string must not be blank" }
        return repository.store(newString)
    }

    fun resetString() {
        repository.delete()
    }
}

@Fundef
fun Routing.controller(service: Service) = route("string") {
    get {
        service.getString().let {
            if (it == null) {
                call.respondText("Not found", status = HttpStatusCode.NotFound)
            } else {
                call.respondText(it)
            }
        }
    } describe {
        summary = "Get the current string value"
        200 response {
            description = "Success"
            plainText { schema<String>() }
        }

        404 response {
            description = "No value currently available"
        }
    }

    put {
        val input = call.receiveText()
        runCatching { service.updateString(input) }
            .onFailure { call.respondText(it.message ?: "Error", status = HttpStatusCode.BadRequest) }
            .onSuccess { call.respondText(it) }
    } describe {
        summary = "Set the string value"
        body { plainText { schema<String>() } }

        200 response {
            description = "Set successfully"
        }

        400 response {
            description = "Invalid value"
        }
    }

    delete {
        service.resetString()
        call.respondText("Deleted", status = HttpStatusCode.OK)
    } describe {
        summary = "Reset the string value"
        200 response {
            description = "Reset successfully"
        }
    }
}

@Fundef
fun Application.openApi() {
    describe {
        title = "Fundef example"
        description = "An example of a Tegral application that uses fundefs to define modules, etc."
    }
}

fun app() = tegral {
    install(WebControllersFeature) {
        enableFundefs = true
    }

    install(OpenApiFeature)
    put(Application::openApi)

    put(Routing::controller)
    put(::Service)
    put(::Repository)
}

fun main() {
    app()
}
