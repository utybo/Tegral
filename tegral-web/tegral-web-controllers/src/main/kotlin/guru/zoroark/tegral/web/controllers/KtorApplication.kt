package guru.zoroark.tegral.web.controllers

import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.services.api.TegralService
import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val STOP_GRACE_PERIOD_MS = 1000L
private const val STOP_TIMEOUT_MS = 2000L

/**
 * Superclass for Ktor applications that use the `shedinja-ktor` extension.
 */
abstract class KtorApplication(
    scope: InjectionScope,
    /**
     * The name of this application. Modules are only installed if their [KtorModule.restrictToAppName] matches this
     * value. By default, both `appName` and [KtorModule.restrictToAppName] are set to `null`. `null` is just the
     * default value and does not have any specific meaning (it does NOT mean "put this module everywhere").
     */
    val appName: String? = null
) : TegralService, KtorModule(Int.MAX_VALUE, appName) {
    private val ktorExtension: KtorExtension by scope.meta()

    /**
     * Settings used for building the application. These are the same as the ones provided as parameters to
     * Ktor's `embeddedServer` function.
     */
    abstract val settings: KtorApplicationSettings<*, *>

    private var application: ApplicationEngine? = null

    /**
     * Function that is called after the Ktor application is created but before any module or controller is called. You
     * can use this function to set up required basic features.
     */
    abstract fun Application.setup()

    override fun Application.install() { setup() }

    override suspend fun start() {
        val app = settings.embeddedServerFromSettings {
            ktorExtension.getModulesForAppName(appName).forEach {
                with(it) { install() }
            }
        }
        application = app
        app.start()
    }

    override suspend fun stop(): Unit = withContext(Dispatchers.IO) {
        application?.stop(STOP_GRACE_PERIOD_MS, STOP_TIMEOUT_MS)
    }
}
