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
    override fun check(modules: List<InjectableModule>) {
        val env = tegralDi(DependencyTrackingInjectionEnvironment) {
            modules.forEach { put(it) }
        }
        // Check for cycles with a simple DFS. Can be optimized to a better algorithm.
        val trace: Deque<Identifier<*>> = LinkedList()
        val visited = mutableSetOf<Pair<Identifier<*>, Identifier<*>>>()

        fun dfs(from: Identifier<*>) {
            if (from in trace) {
                trace.push(from)
                throw TegralDiCheckException(
                    "'noCycle' check failed.\nCyclic dependency found:\n" +
                        trace.reversed().dropWhile { it != from }
                            .joinToString(prefix = "    ", separator = "\n--> ", postfix = "\n") +
                        "Note: --> represents an injection (i.e. A --> B means 'A depends on B')."
                )
            }
            trace.push(from)
            env.dependencies.getValue(from).filter { (from to it) !in visited }.forEach {
                visited.add(from to it)
                dfs(it)
            }
            trace.pop()
        }

        for ((identifier, _) in env.dependencies) {
            trace.clear()
            dfs(identifier)
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
