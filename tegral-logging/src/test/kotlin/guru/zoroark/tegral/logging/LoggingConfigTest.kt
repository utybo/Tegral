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

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addPathSource
import guru.zoroark.tegral.config.core.SectionedConfigurationDecoder
import guru.zoroark.tegral.config.core.TegralConfig
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

data class SimpleTegralContainer(val tegral: TegralConfig)

class LoggingConfigTest {
    private fun parseConfig(toml: String): LoggingConfig {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val path = fs.getPath("/test.toml")
        Files.writeString(path, toml)

        val config = ConfigLoaderBuilder.default()
            .strict()
            .addDecoder(
                SectionedConfigurationDecoder(
                    TegralConfig::class,
                    ::TegralConfig,
                    listOf(LoggingConfig)
                )
            )
            .addPathSource(path)
            .build()
            .loadConfigOrThrow<SimpleTegralContainer>()
        return config.tegral[LoggingConfig]
    }

    @Test
    fun `Default configuration`() {
        val toml = "[tegral]"
        val config = parseConfig(toml)
        val expectedConfig = LoggingConfig(level = LogLevel.Info, loggers = emptyMap())
        assertEquals(expectedConfig, config)
    }

    @Test
    fun `Without custom loggers`() {
        val toml = """
            [tegral.logging]
            level = "Debug"
        """.trimIndent()
        val config = parseConfig(toml)
        val expectedConfig = LoggingConfig(
            level = LogLevel.Debug,
            loggers = emptyMap()
        )
        assertEquals(expectedConfig, config)
    }

    @Test
    fun `With single custom logger, without default level`() {
        val toml = """
            [tegral.logging.loggers."my.test.logger"]
            level = "Warn"
        """.trimIndent()
        val config = parseConfig(toml)
        val expectedConfig = LoggingConfig(
            level = LogLevel.Info,
            loggers = mapOf(
                "my.test.logger" to LoggerConfig(
                    level = LogLevel.Warn
                )
            )
        )
        assertEquals(expectedConfig, config)
    }

    @Test
    fun `With single custom logger, with default level`() {
        val toml = """
            [tegral.logging]
            level = "Debug"

            [tegral.logging.loggers."my.test.logger"]
            level = "Warn"
        """.trimIndent()
        val config = parseConfig(toml)
        val expectedConfig = LoggingConfig(
            level = LogLevel.Debug,
            loggers = mapOf(
                "my.test.logger" to LoggerConfig(
                    level = LogLevel.Warn
                )
            )
        )
        assertEquals(expectedConfig, config)
    }

    @Test
    fun `With many custom loggers, without default level`() {
        val toml = """
            [tegral.logging.loggers."my.test.logger"]
            level = "Warn"

            [tegral.logging.loggers."my.test.logger.subone"]
            level = "Error"
            
            [tegral.logging.loggers."my.test.logger.subtwo"]
            level = "Off"
            
            [tegral.logging.loggers."unrelated.logger"]
            level = "Debug"
            
            [tegral.logging.loggers.noDotInTheName]
            level = "Trace"
        """.trimIndent()
        val config = parseConfig(toml)
        val expectedConfig = LoggingConfig(
            level = LogLevel.Info,
            loggers = mapOf(
                "my.test.logger" to LoggerConfig(
                    level = LogLevel.Warn
                ),
                "my.test.logger.subone" to LoggerConfig(
                    level = LogLevel.Error
                ),
                "my.test.logger.subtwo" to LoggerConfig(
                    level = LogLevel.Off
                ),
                "unrelated.logger" to LoggerConfig(
                    level = LogLevel.Debug
                ),
                "noDotInTheName" to LoggerConfig(
                    level = LogLevel.Trace
                )
            )
        )
        assertEquals(expectedConfig, config)
    }

    @Test
    fun `With many custom loggers, with default level`() {
        val toml = """
            [tegral.logging]
            level = "All"

            [tegral.logging.loggers."my.test.logger"]
            level = "Warn"

            [tegral.logging.loggers."my.test.logger.subone"]
            level = "Error"
            
            [tegral.logging.loggers."my.test.logger.subtwo"]
            level = "Off"
            
            [tegral.logging.loggers."unrelated.logger"]
            level = "Debug"
            
            [tegral.logging.loggers.noDotInTheName]
            level = "Trace"
        """.trimIndent()
        val config = parseConfig(toml)
        val expectedConfig = LoggingConfig(
            level = LogLevel.All,
            loggers = mapOf(
                "my.test.logger" to LoggerConfig(
                    level = LogLevel.Warn
                ),
                "my.test.logger.subone" to LoggerConfig(
                    level = LogLevel.Error
                ),
                "my.test.logger.subtwo" to LoggerConfig(
                    level = LogLevel.Off
                ),
                "unrelated.logger" to LoggerConfig(
                    level = LogLevel.Debug
                ),
                "noDotInTheName" to LoggerConfig(
                    level = LogLevel.Trace
                )
            )
        )
        assertEquals(expectedConfig, config)
    }
}
