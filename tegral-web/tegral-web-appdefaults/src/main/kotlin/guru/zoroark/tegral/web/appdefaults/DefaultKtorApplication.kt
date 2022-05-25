package guru.zoroark.tegral.web.appdefaults

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import guru.zoroark.tegral.config.core.TegralConfig
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.web.config.WebConfiguration
import guru.zoroark.tegral.web.controllers.KtorApplication
import guru.zoroark.tegral.web.controllers.KtorApplicationSettings
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation

/**
 * A basic implementation of [KtorApplication] that uses sane defaults.
 *
 * This implementation sets up the following:
 *
 * - Adds the ContentNegotiation feature to the application with Jackson (and sets up the JavaTimeModule on Jackson)
 * - Uses Netty as the server backend
 * - Uses the [WebConfiguration] to configure the host and port of the application
 */
open class DefaultKtorApplication(scope: InjectionScope) : KtorApplication(scope) {
    private val tegralConfig: TegralConfig by scope()

    override val settings
        get() = KtorApplicationSettings(
            Netty,
            port = tegralConfig[WebConfiguration].port,
            host = tegralConfig[WebConfiguration].host
        )

    override fun Application.setup() {
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
            }
        }
    }
}
