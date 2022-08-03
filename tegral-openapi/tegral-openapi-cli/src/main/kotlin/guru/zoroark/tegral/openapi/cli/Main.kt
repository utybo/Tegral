package guru.zoroark.tegral.openapi.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import guru.zoroark.tegral.openapi.dsl.toJson
import guru.zoroark.tegral.openapi.scripthost.OpenApiScriptHost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.exists
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic

class DumpJson : CliktCommand() {
    val file by argument()
    val output by option("--output", "-o", help = "Output file")

    private val logger = LoggerFactory.getLogger("openapi.dump")

    override fun run(): Unit = runBlocking {
        val fileObject = File(file)
        val openapi = OpenApiScriptHost.compileScript(fileObject, logger::info)
        if (openapi is ResultWithDiagnostics.Success) {
            val outFile = output
            if (outFile == null) {
                logger.info("Output:\n" + openapi.value.toJson())
            } else {
                withContext(Dispatchers.IO) {
                    Files.writeString(Path.of(outFile), openapi.value.toJson(), StandardOpenOption.CREATE)
                }
            }
        } else {
            val compileLogger = LoggerFactory.getLogger("compiler")
            openapi.reports.forEach {
                val actualMessage = it.toFullMessage(fileObject)
                compileLogger.logWithSeverity(it.severity, actualMessage)
            }
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

private fun ScriptDiagnostic.toFullMessage(fileObj: File): String =
    if (location != null) {
        "${message}\nat ${fileObj.absolutePath}:${location!!.start.line}:${location!!.start.col}"
    } else {
        message
    }

fun main(args: Array<String>) {
    applyMinimalistLoggingOverrides()
    DumpJson().main(args)
}
