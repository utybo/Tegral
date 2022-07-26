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

import guru.zoroark.tegral.config.core.ConfigurationSection
import guru.zoroark.tegral.config.core.SectionOptionality

/**
 * The `[tegral.logging]` configuration object.
 */
data class LoggingConfig(
    /**
     * Configuration overrides for individual loggers.
     */
    val loggers: Map<String, LoggerConfig> = mapOf(),
    /**
     * The global log level.
     */
    val level: LogLevel = LogLevel.Info
) {
    companion object : ConfigurationSection<LoggingConfig>(
        "logging",
        SectionOptionality.Optional(LoggingConfig()),
        LoggingConfig::class
    )
}

/**
 * Configuration for an individual logger.
 */
data class LoggerConfig(
    /**
     * The minimum level for this logger (and sub-loggers) to produce output.
     */
    val level: LogLevel
)

/**
 * The log levels.
 */
enum class LogLevel {
    /**
     * Log everything
     */
    All,

    /**
     * Log trace messages and above.
     */
    Trace,

    /**
     * Log debug messages and above.
     */
    Debug,

    /**
     * Log info messages and above.
     */
    Info,

    /**
     * Log warning messages and above.
     */
    Warn,

    /**
     * Log error messages and above.
     */
    Error,

    /**
     * Do not log any message.
     */
    Off
}
