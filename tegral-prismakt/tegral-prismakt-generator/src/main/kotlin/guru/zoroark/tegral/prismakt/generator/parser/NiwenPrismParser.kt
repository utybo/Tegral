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

package guru.zoroark.tegral.prismakt.generator.parser

import guru.zoroark.tegral.niwen.parser.NiwenParser
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

private val logger = LoggerFactory.getLogger("prismakt.parser")

/**
 * A class that exposes [NiwenPrism]'s capabilities as a higher-level class
 */
class NiwenPrismParser {

    private fun createErrorMessage(msg: String) =
        "$msg. Resulting models may be less accurate. Please consider reporting this."

    /**
     * Parse a Prisma schema at the provided [path].
     *
     * Will also dump debugging information if [enableDebugging] is set to true.
     */
    @OptIn(ExperimentalTime::class)
    fun parseOrNull(path: Path, enableDebugging: Boolean): PRoot? = runCatching {
        logger.debug("Reading Prisma file $path")
        val fileText = path.readText()

        logger.debug("Running Niwen Lexer")
        val (tokens, lexerTime) = measureTimedValue { NiwenPrism.tokenize(fileText) }
        logger.debug("Lexing took $lexerTime")

        if (enableDebugging) logger.info("Parser debugging is enabled. Parsing will take more time.")
        logger.debug("Running Niwen Parser")
        val (parserResult, parserTime) = measureTimedValue {
            if (enableDebugging) NiwenPrism.parseDebug(tokens) else NiwenPrism.parse(tokens)
        }
        logger.debug("Parsing took $parserTime")

        if (enableDebugging) {
            runCatching {
                val file = Files.createTempFile("prsmkt-", ".yml")
                file.writeText(parserResult.debuggerResult)
                logger.info("Debug dump available in $file")
            }.onFailure {
                logger.warn("Failed to write debug dump", it)
            }
        }

        when (parserResult) {
            is NiwenParser.ParserResult.Failure -> {
                logger.warn(createErrorMessage("Re-parsing failed: ${parserResult.reason}"))
                return null
            }

            is NiwenParser.ParserResult.Success -> {
                return parserResult.result
            }
        }
    }.getOrElse {
        logger.warn(createErrorMessage("Secondary parsing pass failed"), it)
        null
    }
}
