package guru.zoroark.tegral.openapi.scripthost

import guru.zoroark.tegral.openapi.dsl.RootBuilder
import guru.zoroark.tegral.openapi.dsl.RootDsl
import guru.zoroark.tegral.openapi.dsl.SimpleDslContext
import guru.zoroark.tegral.openapi.dsl.openApi
import guru.zoroark.tegral.openapi.scriptdef.OpenApiScript
import io.swagger.v3.oas.models.OpenAPI
import java.io.File
import java.nio.file.Path
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.api.onSuccess
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

object OpenApiScriptHost {
    suspend fun compileScript(file: File, messageHandler: (String) -> Unit): ResultWithDiagnostics<OpenAPI> =
        compileScript(file.toScriptSource(), messageHandler)

    suspend fun compileScript(source: SourceCode, messageHandler: (String) -> Unit): ResultWithDiagnostics<OpenAPI> {
        val host = BasicJvmScriptingHost()
        val compilationConfig = createJvmCompilationConfigurationFromTemplate<OpenApiScript>()
        messageHandler("Compiling script...")
        return host.compiler(source, compilationConfig).onSuccess {
            val context = SimpleDslContext()
            val builder = RootBuilder(context)
            val evaluationConfig = createOpenApiEvaluationConfig(builder)
            messageHandler("Evaluating script...")
            host.evaluator(it, evaluationConfig).onSuccess {
                val openApi = builder.build()
                context.persistTo(openApi)
                ResultWithDiagnostics.Success(openApi)
            }
        }
    }

    private fun createOpenApiEvaluationConfig(builder: RootBuilder): ScriptEvaluationConfiguration =
        createJvmEvaluationConfigurationFromTemplate<OpenApiScript> {
            implicitReceivers(builder)
        }
}
