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

package guru.zoroark.tegral.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import guru.zoroark.tegral.config.core.RootConfig
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.factory.putFactory
import guru.zoroark.tegral.featureful.ConfigurableFeature
import guru.zoroark.tegral.featureful.LifecycleHookedFeature
import org.slf4j.LoggerFactory
import ch.qos.logback.classic.Logger as LogbackLogger
import org.slf4j.Logger as Slf4jLogger

/**
 * A feature that adds logging support to the application via the `by scope.factory()` syntax.
 *
 * I.e., adds a `Logger` factory to the application.
 */
object LoggingFeature : ConfigurableFeature, LifecycleHookedFeature {
    override val id = "tegral-logging"
    override val name = "Tegral Logging"
    override val description = "Provides logging utilities for Tegral applications"
    override val configurationSections = listOf(LoggingConfig)

    override fun ExtensibleContextBuilderDsl.install() {
        putFactory<Slf4jLogger> { requester -> LoggerFactory.getLogger(requester::class.loggerName) }
    }

    // Setup for the LoggingFeature is done right after the configuration is loaded and not during the regular "service
    // start" phase because order during startup is not guaranteed (by design), yet services may want to log some
    // output.
    override fun onConfigurationLoaded(configuration: RootConfig) {
        val loggingConfig = configuration.tegral[LoggingConfig]
        val context = LoggerFactory.getILoggerFactory() as LoggerContext

        // Configure root logger
        val rootLogger = context.getLogger(LogbackLogger.ROOT_LOGGER_NAME)
        rootLogger.level = loggingConfig.level.toLogbackLevel()

        // Configure individual specified loggers
        loggingConfig.loggers.forEach { (loggerName, loggerConfig) ->
            val logger = context.getLogger(loggerName)
            logger.level = loggerConfig.level.toLogbackLevel()
        }
    }
}

private fun LogLevel.toLogbackLevel(): Level = when(this) {
    LogLevel.ALL -> Level.ALL
    LogLevel.TRACE -> Level.TRACE
    LogLevel.DEBUG -> Level.DEBUG
    LogLevel.INFO -> Level.INFO
    LogLevel.WARN -> Level.WARN
    LogLevel.ERROR -> Level.ERROR
    LogLevel.OFF -> Level.OFF
}
