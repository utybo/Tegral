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

package guru.zoroark.tegral.di.test.check

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.environment.named

private object CompleteCheck : TegralDiCheck {
    override fun check(modules: List<InjectableModule>) {
        val env = tegralDi(DependencyTrackingInjectionEnvironment) {
            modules.forEach { put(it) }
        }
        val deps = env.dependencies
        val requirementToMissingDependency = deps
            .mapValues { (_, v) ->
                v.copy(
                    dependencies = v.dependencies.filter { requirement -> !deps.containsKey(requirement) }
                )
            }
            .filterValues { it.dependencies.isNotEmpty() }
            .ifEmpty { return }

        val missingDependencyToRequesters = requirementToMissingDependency.asSequence()
            .flatMap { (requester, depsInfo) -> depsInfo.dependencies.map { it to (depsInfo.kind to requester) } }
            .associateByMultiPair()
        val suffix =
            if (deps.values.any { it.kind == DependencyKind.Resolution }) {
                "\n\n(--> Injection dependency, R-> Resolution dependency (e.g. alias))"
            } else {
                ""
            }

        val message = missingDependencyToRequesters.entries
            .joinToString(
                prefix = "'complete' check failed.\n" +
                    "Some dependencies were not found. Make sure they are present within your module " +
                    "definitions.\n",
                separator = "\n",
                postfix = suffix
            ) { (k, v) ->
                v.joinToString(
                    prefix = "==> $k not found\n" +
                        "    Requested by:\n",
                    separator = "\n"
                ) { (kind, requester) ->
                    val arrow = when (kind) {
                        DependencyKind.Injection -> "-->"
                        DependencyKind.Resolution -> "R->"
                    }
                    "    $arrow $requester"
                }
            }
        throw TegralDiCheckException(message)
    }
}

/**
 * Checks that the modules are complete and that all dependencies and injections can be properly resolved.
 *
 * You should almost always use this check within your `tegralDiCheck` block, as the situation of a dependency not being
 * met is rarely beneficial and is usually a sign of a coding error (i.e. forgot to put a dependency in the module
 * definitions, typo in a [named] call...).
 */
@TegralDsl
fun TegralDiCheckDsl.complete() {
    checks.add(CompleteCheck)
}
