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

package guru.zoroark.tegral.openapi.scriptdef

import guru.zoroark.tegral.openapi.dsl.RootDsl
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.jvmTarget

/**
 * [Script definition](https://github.com/Kotlin/KEEP/blob/master/proposals/scripting-support.md#script-definition)
 * for the OpenAPI script files (.openapi.kts).
 *
 * Note that this is just a script definition. Please refer to the `tegral-openapi-scripthost` module if you wish
 * compile such files within your application.
 */
@KotlinScript(
    displayName = "Tegral OpenAPI definition script",
    fileExtension = "openapi.kts",
    compilationConfiguration = OpenApiScriptCompilationConfig::class
)
@Suppress("UnnecessaryAbstractClass") // required because this is a script definition
abstract class OpenApiScript

/**
 * Default compilation configuration for [OpenApiScript].
 */
object OpenApiScriptCompilationConfig : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
        jvmTarget("11")
    }
    defaultImports("guru.zoroark.tegral.openapi.dsl.*")
    implicitReceivers(RootDsl::class)
})
