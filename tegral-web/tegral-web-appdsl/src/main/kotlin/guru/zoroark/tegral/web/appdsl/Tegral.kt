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

package guru.zoroark.tegral.web.appdsl

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.web.appdefaults.applyLoggingOverrides
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val logger = LoggerFactory.getLogger("tegral.web.appdsl")
private val statsLogger = LoggerFactory.getLogger("tegral.web.appdsl.stats")

private data class StartPhase(val name: String, val duration: Duration)

private data class ApplicationStartResult(
    val application: TegralApplication,
    val phases: List<StartPhase>
)

/**
 * Starting block for creating, configuring and launching a Tegral application. This function:
 *
 * - Creates a [TegralApplicationBuilder]
 * - Applies [sane defaults][applyDefaults] to this builder
 * - Runs the provided lambda in this builder.
 * - Builds the application.
 * - Starts the application.
 * - Returns the application object.
 *
 * @param enableLoggingOverrides Set to false if you customize logback's configuration yourself. Otherwise, AppDSL will
 * configure Logback with sensible defaults.
 */
@OptIn(ExperimentalTime::class)
@TegralDsl
fun tegral(
    enableLoggingOverrides: Boolean = true,
    enableDefaults: Boolean = true,
    block: TegralApplicationDsl.() -> Unit
): TegralApplication {
    if (enableLoggingOverrides) applyLoggingOverrides()
    logger.info("Initializing...")

    val (appStartResult, buildTime) = measureTimedValue {
        logger.debug("==> Configuring application from tegral block...")
        val (builder, configDuration) = measureTimedValue {
            val builder = TegralApplicationBuilder()
            if (enableDefaults) builder.applyDefaults()
            block(builder)
            builder
        }

        logger.debug("==> Building application...")
        val (application, buildDuration) = measureTimedValue {
            builder.build()
        }

        logger.debug("==> Application environment built, starting.")
        logger.debug("Running pre-start hooks")
        val preStartDuration = measureTime {
            application.lifecycleFeatures.forEach { it.beforeStart(application.environment) }
        }

        logger.debug("Starting application")
        val appStartDuration = measureTime {
            runBlocking { application.start() }
        }
        ApplicationStartResult(
            application,
            listOf(
                StartPhase("Configuration", configDuration),
                StartPhase("Build", buildDuration),
                StartPhase("Pre-start", preStartDuration),
                StartPhase("Start", appStartDuration)
            )
        )
    }

    val maxPhaseNameLength = appStartResult.phases.maxOf { it.name.length }
    logger.info(
        "Application started in ${buildTime.secondsWithMs}. Enable debug logging on 'tegral.web.appdsl.stats' for " +
            "more details."
    )
    statsLogger.debugString {
        appendLine("Start sequence breakdown:")
        appendLine(
            appStartResult.phases.joinToString(separator = "\n") {
                "- ${it.name.padStart(maxPhaseNameLength)}: ${it.duration.secondsWithMs}"
            }
        )
        val miscTime = buildTime - appStartResult.phases.sumOf { it.duration }
        append("Misc. time spent in-between phases: ${miscTime.secondsWithMs}")
    }
    return appStartResult.application
}

private inline fun <T> Iterable<T>.sumOf(transformer: (T) -> Duration): Duration =
    fold(Duration.ZERO) { a, b -> a + transformer(b) }

private inline fun Logger.debugString(builder: StringBuilder.() -> Unit) {
    if (isDebugEnabled) {
        debug(buildString(builder))
    }
}

private val Duration.secondsWithMs: String
    get() {
        val seconds = this.toDouble(DurationUnit.SECONDS)
        return String.format(Locale.ENGLISH, "%.3f s", seconds)
    }
