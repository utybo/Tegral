package guru.zoroark.tegral.web.apptest

import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.services.api.TegralService
import guru.zoroark.tegral.web.controllers.KtorExtension
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.ClientProvider

open class DefaultKtorTestApplication(scope: InjectionScope) : KtorTestApplication(scope, null)

abstract class KtorTestApplication(scope: InjectionScope, val appName: String?) : TegralService, ClientProvider {
    private val ktorExtension: KtorExtension by scope.meta()

    private val applicationTestBuilder = ApplicationTestBuilder()

    override suspend fun start() {
        val modules = ktorExtension.getModulesForAppName(appName)
        modules.forEach { module ->
            applicationTestBuilder.application {
                with(module) { install() }
            }
        }
    }

    override suspend fun stop() {
        // No-op
    }

    override fun createClient(block: HttpClientConfig<out HttpClientEngineConfig>.() -> Unit): HttpClient =
        applicationTestBuilder.createClient(block)

    override val client: HttpClient get() = applicationTestBuilder.client
}
