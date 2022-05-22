package guru.zoroark.tegral.web.appdsl

import guru.zoroark.tegral.core.TegralDsl
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("tegral.web.appdsl.tegralblock")

@TegralDsl
fun tegral(block: TegralApplicationDsl.() -> Unit): TegralApplication {
    logger.debug("Configuring application from tegral block...")
    val builder = TegralApplicationBuilder()
    builder.applyDefaults()
    block(builder)

    logger.debug("Building application...")
    val application = builder.build()
    logger.debug("Application environment built, starting.")
    application.start()
    return application
}
