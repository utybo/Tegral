/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package guru.zoroark.tegral.utils.logtools

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.pattern.ThrowableProxyConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.LayoutBase
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import org.slf4j.LoggerFactory

private const val MAX_LOGGER_SIZE = 20

/**
 * A layout for [logback](https://logback.ch) with a pleasant, colorful scheme, specifically intended for CLI tools.
 *
 * Use [applyMinimalistLoggingOverrides] to use this.
 */
class MinimalistLogs : LayoutBase<ILoggingEvent>() {
    private val throwableProxyConverter = ThrowableProxyConverter()

    init {
        throwableProxyConverter.start()
    }

    override fun doLayout(event: ILoggingEvent): String =
        buildString {
            appendPreLevelSymbol(event.level)
            append('[')
            append(event.level.asCharacter())
            append("] ")
            appendPostLevelSymbol(event.level)
            append(event.loggerName.resize(MAX_LOGGER_SIZE))
            append(" - ")
            append(event.formattedMessage.replace("\n", "\n    "))
            append("\n")
            val throwableProxy = event.throwableProxy
            if (throwableProxy != null) {
                val rawMessage = throwableProxyConverter.convert(event).replace(System.lineSeparator(), "\n    ")
                append("    $rawMessage")
            }
            appendPostMessage(event.level)
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

    // https://en.wikipedia.org/wiki/ANSI_escape_code#SGR
    private fun sgr(content: String) = 0x1B.toChar() + "[" + content + "m"
    private val grayFg = "37"
    private val redFg = "31"
    private val yellowFg = "33"
    private val blueFg = "94"
    private val reset = "0"

    private fun StringBuilder.appendPreLevelSymbol(level: Level) {
        when (level) {
            Level.TRACE -> append(sgr(grayFg))
            Level.DEBUG -> append(sgr(grayFg))
            Level.INFO -> append(sgr(blueFg))
            Level.WARN -> append(sgr(yellowFg))
            Level.ERROR -> append(sgr(redFg))
        }
    }

    private fun StringBuilder.appendPostLevelSymbol(level: Level) {
        if (level == Level.INFO) {
            append(sgr(reset))
        }
    }

    private fun StringBuilder.appendPostMessage(level: Level) {
        if (level in setOf(Level.TRACE, Level.DEBUG, Level.WARN, Level.DEBUG)) {
            append(sgr(reset))
        }
    }
}

/**
 * Applies the [MinimalistLogs] layout as well as basic layout settings to Logback.
 */
fun applyMinimalistLoggingOverrides(level: Level = Level.INFO) {
    val ctx = (LoggerFactory.getILoggerFactory() as LoggerContext)
    val rootLogger = ctx.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.level = level

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
