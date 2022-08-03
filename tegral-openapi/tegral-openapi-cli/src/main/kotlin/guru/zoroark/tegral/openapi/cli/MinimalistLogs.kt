package guru.zoroark.tegral.openapi.cli

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.pattern.ThrowableProxyConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.LayoutBase
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import org.slf4j.LoggerFactory

class MinimalistLogs : LayoutBase<ILoggingEvent>() {
    private val throwableProxyConverter = ThrowableProxyConverter()
    init {
        throwableProxyConverter.start()
    }

    override fun doLayout(event: ILoggingEvent): String =
        buildString {
            append('[')
            append(event.level.asCharacter())
            append("] ")
            append(event.loggerName.resize(20))
            append(" - ")
            append(event.formattedMessage.replace("\n", "\n    "))
            append("\n")
            val throwableProxy = event.throwableProxy
            if (throwableProxy != null) {
                val rawMessage = throwableProxyConverter.convert(event).replace(System.lineSeparator(), "\n    ");
                append("    $rawMessage")
            }
        }

    private fun Level.asCharacter() = when (this) {
        Level.TRACE -> 't'
        Level.DEBUG -> 'd'
        Level.INFO -> 'i'
        Level.WARN -> '!'
        Level.ERROR -> 'X'
        Level.OFF -> 'X'
        else -> ' '
    }

    private fun String.resize(size: Int): String {
        val str = padEnd(size)
        return if (str.length > size) {
            str.substring(0, size)
        } else {
            str
        }
    }
}

fun applyMinimalistLoggingOverrides() {
    val ctx = (LoggerFactory.getILoggerFactory() as LoggerContext)
    val rootLogger = ctx.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.level = Level.INFO

    val le = LayoutWrappingEncoder<ILoggingEvent>().apply {
        context = ctx
        layout = MinimalistLogs()
    }
    le.start()

    for (appender in rootLogger.iteratorForAppenders()) {
        if (appender is ConsoleAppender<*>) {
            appender.encoder = le
        }
    }
}
