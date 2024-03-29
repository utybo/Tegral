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
import guru.zoroark.tegral.di.TegralDiException
import guru.zoroark.tegral.di.environment.InjectableModule

/**
 * Exception type for check failures (i.e. when a check is not met).
 */
class TegralDiCheckException(message: String) : TegralDiException(message)

/**
 * An individual Tegral DI check that can be ran.
 */
fun interface TegralDiCheck {
    /**
     * Run this check on the given list of modules.
     */
    fun check(modules: List<InjectableModule>)
}

/**
 * DSL receiver class for the Tegral DI check DSL.
 */
@TegralDsl
class TegralDiCheckDsl {
    /**
     * Modules that should be taken into account during the checks
     */
    val modules = mutableListOf<InjectableModule>()

    /**
     * Checks that should be ran.
     */
    val checks = mutableListOf<TegralDiCheck>()
}

/**
 * Adds the given modules to this Tegral DI check instance.
 */
@TegralDsl
fun TegralDiCheckDsl.modules(vararg modules: InjectableModule) {
    this.modules.addAll(modules)
}

/**
 * DSL for checking your Tegral DI modules.
 *
 * Imagine that we have three modules, `web`, `db` and `generate`. A typical use case would look like:
 *
 * ```
 * @Test
 * fun `Tegral DI checks`() = tegralDiCheck {
 *     modules(web, db, generate)
 *
 *     +complete
 *     +noCycle
 *     +safeInjection
 * }
 * ```
 *
 * Note that running checks will instantiate the classes within the modules in order to trigger the `by scope()`
 * injections.
 */
@TegralDsl
fun tegralDiCheck(block: TegralDiCheckDsl.() -> Unit) {
    TegralDiCheckDsl().apply(block).check()
}

private fun TegralDiCheckDsl.check() {
    if (checks.isEmpty()) {
        throw TegralDiCheckException(
            """
            tegralDiCheck called without any rule, which checks nothing.
            --> Add rules using ruleName() (for example 'complete()')
            --> If you do not want to run any checks, remove the tegralDiCheck block entirely.
            For more information, visit https://tegral.zoroark.guru/docs/modules/core/di/testing/checks
            """.trimIndent()
        )
    } else {
        checks.forEach { it.check(modules) }
    }
}

internal fun <K, V> Sequence<Pair<K, V>>.associateByMultiPair(): Map<K, List<V>> =
    fold(mutableMapOf<K, MutableList<V>>()) { map, (missing, requester) ->
        map.compute(missing) { _, original ->
            (original ?: mutableListOf()).apply { add(requester) }
        }
        map
    }
