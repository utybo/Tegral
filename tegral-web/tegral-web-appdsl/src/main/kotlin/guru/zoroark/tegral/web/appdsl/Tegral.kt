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
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("tegral.web.appdsl.tegralblock")

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
@TegralDsl
fun tegral(
    enableLoggingOverrides: Boolean = true,
    enableDefaults: Boolean = true,
    block: TegralApplicationDsl.() -> Unit
): TegralApplication {
    if (enableLoggingOverrides) applyLoggingOverrides()

    logger.info("Configuring application from tegral block...")
    val builder = TegralApplicationBuilder()
    if (enableDefaults) builder.applyDefaults()
    block(builder)

    logger.info("Building application...")
    val application = builder.build()
    logger.info("Application environment built, starting.")
    runBlocking { application.start() }
    return application
}
