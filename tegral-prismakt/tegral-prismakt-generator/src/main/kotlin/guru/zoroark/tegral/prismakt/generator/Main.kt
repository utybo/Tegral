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

package guru.zoroark.tegral.prismakt.generator

import ch.qos.logback.classic.Level
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.prismakt.generator.generators.ExposedDaoGenerator
import guru.zoroark.tegral.prismakt.generator.generators.ExposedSqlGenerator
import guru.zoroark.tegral.prismakt.generator.parser.NiwenPrismParser
import guru.zoroark.tegral.prismakt.generator.protocol.GeneratorProtocolController
import guru.zoroark.tegral.prismakt.generator.protocol.GeneratorProtocolHandler
import guru.zoroark.tegral.prismakt.generator.protocol.JsonRpcProtocol
import guru.zoroark.tegral.utils.logtools.applyMinimalistLoggingOverrides
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

val genericLogger = LoggerFactory.getLogger("tegral.prismakt")

fun main() {
    applyMinimalistLoggingOverrides(Level.DEBUG)
    try {
        genericLogger.warn("Tegral PrismaKt Generator is EXPERIMENTAL! Use at your own risk!")
        val env = tegralDi {
            put(::JsonRpcProtocol)
            put<GeneratorProtocolHandler>(::GeneratorProtocolController)
            put(::ExposedSqlGenerator)
            put(::ExposedDaoGenerator)
            put { ExecutionContext(true) }
            put(::NiwenPrismParser)
        }
        val protocol = env.get<JsonRpcProtocol>()
        protocol.exchange()
    } catch (ex: Exception) {
        genericLogger.error("Fatal error", ex)
        exitProcess(1);
    }
}
