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

package guru.zoroark.tegral.openapi.scripthost

import guru.zoroark.tegral.openapi.dsl.RootBuilder
import guru.zoroark.tegral.openapi.dsl.SimpleDslContext
import guru.zoroark.tegral.openapi.scriptdef.OpenApiScript
import io.swagger.v3.oas.models.OpenAPI
import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.api.onSuccess
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

/**
 * High-level utilities for compiling `.openapi.kts` scripts.
 *
 * This provides an easier interface over
 * [Kotlin's scripting system](https://github.com/Kotlin/KEEP/blob/master/proposals/scripting-support.md).
 */
object OpenApiScriptHost {
    /**
     * Compiles a `.openapi.kts` script from a file into an [OpenAPI] object.
     *
     * The result is wrapped in a [ResultWithDiagnostics] object, which contains a list of warnings, errors, etc. as
     * well as the object itself.
     */
    suspend fun compileScript(file: File, messageHandler: (String) -> Unit): ResultWithDiagnostics<OpenAPI> =
        compileScript(file.toScriptSource(), messageHandler)

    private suspend fun compileScript(
        source: SourceCode,
        messageHandler: (String) -> Unit
    ): ResultWithDiagnostics<OpenAPI> {
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
