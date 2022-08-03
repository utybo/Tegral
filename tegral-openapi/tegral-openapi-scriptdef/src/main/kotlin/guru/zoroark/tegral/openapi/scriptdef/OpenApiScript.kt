package guru.zoroark.tegral.openapi.scriptdef

import guru.zoroark.tegral.openapi.dsl.RootDsl
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.implicitReceivers
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.util.classpathFromClass

@KotlinScript(
    displayName = "Tegral OpenAPI definition script",
    fileExtension = "openapi.kts",
    compilationConfiguration = OpenApiScriptCompilationConfig::class
)
abstract class OpenApiScript

object OpenApiScriptCompilationConfig : ScriptCompilationConfiguration({
    jvm {
        dependenciesFromCurrentContext(wholeClasspath = true)
    }
    defaultImports("guru.zoroark.tegral.openapi.dsl.*")
    implicitReceivers(RootDsl::class)
})
