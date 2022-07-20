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

import com.sksamuel.hoplite.toml.TomlPropertySource
import guru.zoroark.tegral.config.core.RootConfig
import guru.zoroark.tegral.config.core.TegralConfig
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.services.feature.ServicesFeature
import guru.zoroark.tegral.web.appdsl.install
import guru.zoroark.tegral.web.appdsl.tegral
import org.slf4j.Logger
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class TestApplicationWithCustomLoggers {
    @LoggerName("logged.one")
    class LoggedOne(scope: InjectionScope) {
        private val logger: Logger by scope()

        fun logMessage() {
            logger.debug("Debugging LoggedOne")
            logger.info("Informing LoggedOne")
            logger.warn("Warning LoggedOne")
        }
    }

    @LoggerName("logged.two")
    class LoggedTwo(scope: InjectionScope) {
        private val logger: Logger by scope()

        fun logMessage() {
            logger.debug("Debugging LoggedTwo")
            logger.info("Informing LoggedTwo")
            logger.warn("Warning LoggedTwo")
        }
    }

    @LoggerName("logged.three")
    class LoggedThree(scope: InjectionScope) {
        private val logger: Logger by scope()

        fun logMessage() {
            logger.debug("Debugging LoggedThree")
            logger.info("Informing LoggedThree")
            logger.warn("Warning LoggedThree")
        }
    }

    data class TestConfiguration(override val tegral: TegralConfig) : RootConfig

    @Test
    fun `Test full application`() {
        val config = """
            [tegral.logging]
            level = "Debug"

            [tegral.logging.loggers."logged.two"]
            level = "Info"
            
            [tegral.logging.loggers."logged.three"]
            level = "Warn"
        """.trimIndent()

        val output = captureStdout {
            val app = tegral(enableDefaults = false) {
                install(LoggingFeature)
                install(ServicesFeature)

                useConfiguration(TestConfiguration::class) {
                    addPropertySource(TomlPropertySource(config))
                }

                put(::LoggedOne)
                put(::LoggedTwo)
                put(::LoggedThree)
            }

            app.environment.get<LoggedOne>().logMessage()
            app.environment.get<LoggedTwo>().logMessage()
            app.environment.get<LoggedThree>().logMessage()
        }

        println(output)

        // LoggedOne should output everything
        assertContains(output, "Debugging LoggedOne")
        assertContains(output, "Informing LoggedOne")
        assertContains(output, "Warning LoggedOne")

        // LoggedTwo should contain info and warning
        assertFalse(output.contains("Debugging LoggedTwo"))
        assertContains(output, "Informing LoggedTwo")
        assertContains(output, "Warning LoggedTwo")

        // LoggedThree should contain warning
        assertFalse(output.contains("Debugging LoggedThree"))
        assertFalse(output.contains("Informing LoggedThree"))
        assertContains(output, "Warning LoggedThree")
    }

    class OutputToLogger(scope: InjectionScope) {
        val logger: Logger by scope()
    }

    private fun logForLevel(logLevel: LogLevel, message: String): (Logger) -> Unit {
        return when (logLevel) {
            LogLevel.Trace -> { log: Logger -> log.trace(message) }
            LogLevel.Debug -> { log: Logger -> log.debug(message) }
            LogLevel.Info -> { log: Logger -> log.info(message) }
            LogLevel.Warn -> { log: Logger -> log.warn(message) }
            LogLevel.Error -> { log: Logger -> log.error(message) }
            else -> error("$logLevel is not a valid message level and cannot be converted to a logger call")
        }
    }

    @Test
    fun `Test log levels`() {
        val messageLevels = setOf(LogLevel.Trace, LogLevel.Debug, LogLevel.Info, LogLevel.Warn, LogLevel.Error)
        val logMessages = messageLevels.map { logForLevel(it, "Test message for ${it.name}") }

        data class LogLevelTestCase(val loggerLevel: LogLevel, val expectedOutputs: Set<LogLevel>)

        val testCases = listOf(
            LogLevelTestCase(LogLevel.All, messageLevels),
            LogLevelTestCase(LogLevel.Trace, messageLevels),
            LogLevelTestCase(LogLevel.Debug, messageLevels - LogLevel.Trace),
            LogLevelTestCase(LogLevel.Info, messageLevels - LogLevel.Trace - LogLevel.Debug),
            LogLevelTestCase(LogLevel.Warn, setOf(LogLevel.Warn, LogLevel.Error)),
            LogLevelTestCase(LogLevel.Error, setOf(LogLevel.Error)),
            LogLevelTestCase(LogLevel.Off, emptySet())
        )

        for (test in testCases) {
            val config = """
                [tegral.logging]
                level = "${test.loggerLevel.name}"
            """.trimIndent()
            val output = captureStdout {
                val app = tegral(enableDefaults = false) {
                    install(LoggingFeature)
                    install(ServicesFeature)

                    useConfiguration(TestConfiguration::class) {
                        addPropertySource(TomlPropertySource(config))
                    }

                    put(::OutputToLogger)
                }

                val out = app.environment.get<OutputToLogger>().logger
                logMessages.forEach { it(out) }
            }

            for (shouldBePresent in test.expectedOutputs) {
                assertContains(output, "Test message for ${shouldBePresent.name}")
            }
            for (shouldNotBePresent in messageLevels - test.expectedOutputs) {
                assertFalse(output.contains("Test message for ${shouldNotBePresent.name}"))
            }
        }
    }
}

private inline fun captureStdout(block: () -> Unit): String {
    val originalOut = System.out
    val byteArrayOutputStream = ByteArrayOutputStream()
    val capturingOut = java.io.PrintStream(byteArrayOutputStream)
    try {
        System.setOut(capturingOut)
        block()
    } finally {
        System.setOut(originalOut)
    }
    return String(byteArrayOutputStream.toByteArray())
}
