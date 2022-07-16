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
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import java.util.Deque
import java.util.LinkedList

private object NoCycleCheck : TegralDiCheck {
    private data class DfsEdge(val from: Identifier<*>, val to: Identifier<*>, val kind: DependencyKind)

    override fun check(modules: List<InjectableModule>) {
        val env = tegralDi(DependencyTrackingInjectionEnvironment) {
            modules.forEach { put(it) }
        }
        // Check for cycles with a simple DFS. Can be optimized to a better algorithm.
        val trace: Deque<Pair<Identifier<*>, DependencyKind?>> = LinkedList()
        val visited = mutableSetOf<DfsEdge>()

        fun dfs(from: Identifier<*>, fromEdgeKind: DependencyKind?) {
            if (trace.any { it.first == from }) {
                trace.push(from to fromEdgeKind)
                val traceString = trace
                    .reversed()
                    .dropWhile { it.first != from }
                    .joinToString(separator = "\n") { (id, kind) ->
                        "${kind?.arrow ?: "   "} $id"
                    }
                val resolveKey =
                    if (trace.any { it.second == DependencyKind.Resolution }) {
                        "\n      R-> represents a resolution dependency (e.g. an alias being resolved)."
                    } else {
                        ""
                    }
                val message = "'noCycle' check failed.\nCyclic dependency found:\n" +
                    "$traceString\n\n" +
                    "Note: --> represents an injection (i.e. A --> B means 'A depends on B')." +
                    resolveKey
                throw TegralDiCheckException(message)
            }
            trace.push(from to fromEdgeKind)
            val dependenciesInfo = env.dependencies.getValue(from)
            dependenciesInfo.dependencies
                .map { DfsEdge(from, it, dependenciesInfo.kind) }
                .filter { it !in visited }
                .forEach {
                    visited.add(it)
                    dfs(it.to, it.kind)
                }
            trace.pop()
        }

        for ((identifier, _) in env.dependencies) {
            trace.clear()
            dfs(identifier, null)
        }
    }
}

/**
 * Checks that no cyclic dependencies are present within the modules.
 *
 * A cyclic dependency is a scenario where a class `Foo` depends on itself, either directly (injects itself into itself)
 * or indirectly (For example, `Foo` depends on `Bar`, which in turn depends on `Foo`).
 *
 * Cyclic dependencies are generally well-supported within Tegral DI as long as injections are not done eagerly (see
 * [InjectionEnvironment] for more information). However, they are generally symptoms of badly design systems.
 *
 * Note that this check ignores whether classes are present within a module or not: use the [complete] check for this.
 */
@TegralDsl
fun TegralDiCheckDsl.noCycle() {
    checks.add(NoCycleCheck)
}
