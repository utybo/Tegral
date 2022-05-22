package guru.zoroark.tegral.web.controllers

import io.ktor.server.application.Application
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.ApplicationEngineFactory
import io.ktor.server.engine.embeddedServer
import java.io.File

/**
 * Application settings for the Ktor application. Identical to the parameters of the [embeddedServer] function from
 * Ktor.
 */
class KtorApplicationSettings<TEngine : ApplicationEngine, TConfiguration : ApplicationEngine.Configuration>(
    private val engine: ApplicationEngineFactory<TEngine, TConfiguration>,
    private val port: Int = 80,
    private val host: String = "0.0.0.0",
    private val watchPaths: List<String> = listOf(File(".").canonicalPath),
    private val configure: TConfiguration.() -> Unit = {}
) {
    /**
     * Equivalent to `embeddedServer` but uses this object's information as the parameters.
     */
    fun embeddedServerFromSettings(block: Application.() -> Unit) =
        embeddedServer(engine, port, host, watchPaths, configure, block)
}
