package guru.zoroark.tegral.logging

import guru.zoroark.tegral.config.core.ConfigurationSection
import guru.zoroark.tegral.config.core.SectionOptionality

data class LoggingConfig(
    val loggers: Map<String, LoggerConfig> = mapOf(),
    val level: LogLevel = LogLevel.INFO
) {
    companion object : ConfigurationSection<LoggingConfig>(
        "logging",
        SectionOptionality.Optional(LoggingConfig()),
        LoggingConfig::class
    )
}

data class LoggerConfig(
    val level: LogLevel
)

enum class LogLevel {
    ALL,
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    OFF
}
