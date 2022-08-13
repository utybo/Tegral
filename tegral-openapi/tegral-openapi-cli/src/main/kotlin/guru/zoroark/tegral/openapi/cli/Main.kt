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

package guru.zoroark.tegral.openapi.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.path
import guru.zoroark.tegral.openapi.dsl.OpenApiVersion
import guru.zoroark.tegral.openapi.dsl.toJson
import guru.zoroark.tegral.openapi.dsl.toYaml
import guru.zoroark.tegral.openapi.scripthost.OpenApiScriptHost
import io.swagger.v3.oas.models.OpenAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic

/**
 * The format of the output OpenAPI file.
 */
enum class Format {
    /**
     * Use the JSON format. Will not be beautified.
     */
    JSON,

    /**
     * Use the YAML format. Will be beautified.
     */
    YAML;

    /**
     * Convert the given OpenAPI object into the desired format.
     */
    operator fun invoke(value: OpenAPI, version: OpenApiVersion): String =
        when (this) {
            JSON -> value.toJson(version)
            YAML -> value.toYaml(version)
        }
}

/**
 * Tegral OpenAPI CLI object, which provides the "dump" command (managed by Clikt).
 */
class TegralOpenApiCli(fileSystem: FileSystem = FileSystems.getDefault()) : CliktCommand(name = "tegral-openapi-cli") {
    private val file by argument()
        .path(mustExist = true, canBeDir = false, mustBeReadable = true, fileSystem = fileSystem)
    private val output by option("--output", "-o", help = "Output file")
        .path(canBeDir = false, fileSystem = fileSystem)
    private val quiet by option("--quiet", "-q").flag(default = false)
    private val format by option("--format", "-f", help = "Output format")
        .enum<Format> { it.name.lowercase() }
        .default(Format.JSON)
    private val version by option("--openapi-version", "-a", help = "OpenAPI version")
        .enum<OpenApiVersion> { it.version }
        .default(OpenApiVersion.V3_0)

    private val logger = LoggerFactory.getLogger("openapi.dump")

    override fun run() {
        applyMinimalistLoggingOverrides(quiet = quiet)
        val openapi = runBlocking { OpenApiScriptHost.compileScript(file, logger::info) }
        val compileLogger = LoggerFactory.getLogger("compiler")
        openapi.reports.forEach {
            val actualMessage = it.toFullMessage(file)
            compileLogger.logWithSeverity(it.severity, actualMessage)
        }
        if (openapi is ResultWithDiagnostics.Success) {
            val str = format(openapi.value, version)
            val outFile = output
            if (outFile == null) {
                println(str)
            } else {
                Files.writeString(outFile, str, StandardOpenOption.CREATE)
                logger.info("Output written to ${outFile.toAbsolutePath()}")
            }
        } else {
            throw ProgramResult(1)
        }
    }
}

private fun Logger.logWithSeverity(severity: ScriptDiagnostic.Severity, actualMessage: String) {
    when (severity) {
        ScriptDiagnostic.Severity.DEBUG ->
            debug(actualMessage)

        ScriptDiagnostic.Severity.INFO ->
            info(actualMessage)

        ScriptDiagnostic.Severity.WARNING ->
            warn(actualMessage)

        ScriptDiagnostic.Severity.ERROR ->
            error(actualMessage)

        ScriptDiagnostic.Severity.FATAL ->
            error(actualMessage)
    }
}

private fun ScriptDiagnostic.toFullMessage(path: Path): String =
    if (location != null) {
        "${message}\nat ${path.toAbsolutePath()}:${location!!.start.line}:${location!!.start.col}"
    } else {
        message
    }

/**
 * Main entrypoint for the CLI.
 */
fun main(args: Array<String>) = TegralOpenApiCli().main(args)
