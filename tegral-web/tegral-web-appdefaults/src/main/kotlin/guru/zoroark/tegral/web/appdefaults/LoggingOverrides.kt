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

package guru.zoroark.tegral.web.appdefaults

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.ConsoleAppender
import org.slf4j.LoggerFactory

/**
 * Applies sensible, default settings for logging. Should only be used to get a
 * "base" configuration on top of Logback. Do not use this if you have your own
 * logback.xml file.
 */
fun applyLoggingOverrides() {
    val ctx = (LoggerFactory.getILoggerFactory() as LoggerContext)
    val rootLogger = ctx.getLogger(Logger.ROOT_LOGGER_NAME)
    rootLogger.level = Level.INFO

    val ple = PatternLayoutEncoder().apply {
        context = ctx
        pattern = "%d{YYYY-MM-dd HH:mm:ss.SSS} %highlight(%-5level) %cyan(%logger{36}) - %msg%n"
    }
    ple.start()

    for (appender in rootLogger.iteratorForAppenders())
    {
        if (appender is ConsoleAppender<*>)
        {
            appender.encoder = ple
        }
    }
}
