package guru.zoroark.tegral.di.test.check

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.named

/**
 * Checks that the modules are complete and that all dependencies and injections can be properly resolved.
 *
 * You should almost always use this check within your `tegralDiCheck` block, as the situation of a dependency not being
 * met is rarely beneficial and is usually a sign of a coding error (i.e. forgot to put a dependency in the module
 * definitions, typo in a [named] call...).
 */
@TegralDsl
val complete = IndividualCheck { modules ->
    val env = tegralDi(DependencyTrackingInjectionEnvironment) {
        modules.forEach { put(it) }
    }
    val deps = env.dependencies
    val requirementToMissingDependency = deps
        .mapValues { (_, v) -> v.filter { requirement -> !deps.containsKey(requirement) } }
        .filterValues { it.isNotEmpty() }
        .takeIf { it.isNotEmpty() }
    if (requirementToMissingDependency != null) {
        val message = requirementToMissingDependency.asSequence()
            .flatMap { (requester, missingDependencies) -> missingDependencies.map { it to requester } }
            .associateByMultiPair()
            .entries.joinToString(
                prefix =
                """
                    'complete' check failed.
                    Some dependencies were not found. Make sure they are present within your module definitions.
                """.trimIndent() + "\n",
                separator = "\n"
            ) { (k, v) ->
                v.joinToString(
                    prefix = "--> $k not found\n    Requested by:\n", separator = "\n"
                ) { requester -> "    --> $requester" }
            }
        throw TegralDiCheckException(message)
    }
}
