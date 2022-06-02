package guru.zoroark.tegral.web.appdsl

import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import guru.zoroark.tegral.di.services.services
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * This object represents a built and possibly running Tegral application.
 *
 * You can interact with this application by starting or stopping it, inspecting and retrieving elements from its
 * [DI environment][environment], etc.
 */
class TegralApplication(
    /**
     * The dependency injection environment used in this application. All components of this application live here
     * (either directly or via the [meta-environment][ExtensibleInjectionEnvironment.metaEnvironment]).
     */
    val environment: ExtensibleInjectionEnvironment
) {
    /**
     * Starts this application with all its components.
     *
     * Note that you **do not** need to call this method if you are using the [tegral] block, which takes care of
     * launching the application for you.
     */
    fun start() {
        val logger = LoggerFactory.getLogger("tegral.web.appdsl.start")
        runBlocking { environment.services.startAll(logger::info) }
    }
}
