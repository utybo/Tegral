package guru.zoroark.tegral.web.controllers

import io.ktor.server.application.Application
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing

/**
 * Identical to [KtorModule], but allows you to directly add routes, as if calling `routing` in a [KtorModule].
 */
abstract class KtorController(priority: Int = DEFAULT_CONTROLLER_PRIORITY, restrictToAppName: String? = null) :
    KtorModule(priority, restrictToAppName) {
    /**
     * Install this controller's routes. You can call `route`, `get`, etc here.
     */
    abstract fun Routing.install()

    override fun Application.install() {
        routing { install() }
    }
}
