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
