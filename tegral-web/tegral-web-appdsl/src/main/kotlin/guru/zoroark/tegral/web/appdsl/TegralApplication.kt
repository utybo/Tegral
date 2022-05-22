package guru.zoroark.tegral.web.appdsl

import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import guru.zoroark.tegral.di.services.services
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class TegralApplication(val environment: ExtensibleInjectionEnvironment) {
    fun start() {
        val logger = LoggerFactory.getLogger("tegral.web.appdsl.start")
        runBlocking { environment.services.startAll(logger::info) }
    }
}
