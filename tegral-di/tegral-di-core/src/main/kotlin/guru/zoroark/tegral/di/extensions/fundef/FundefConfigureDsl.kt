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

package guru.zoroark.tegral.di.extensions.fundef

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.environment.Qualifier
import guru.zoroark.tegral.di.environment.ScopedContext
import kotlin.reflect.KFunction

/**
 * DSL object, for use with [configureFundef]. Provides functions to further configure fundefs.
 *
 * @property function The function to configure and use for the fundef
 */
@ExperimentalFundef
class FundefConfigureDsl<R>(val function: KFunction<R>) {
    /**
     * Qualifiers registered for this fundef via [qualifyWith]
     */
    val qualifiers = mutableMapOf<String, Qualifier>()

    /**
     * Set the qualifier for the parameter with the name of the given string
     */
    @TegralDsl
    infix fun String.qualifyWith(qualifier: Qualifier) {
        qualifiers[this] = qualifier
    }

    /**
     * Create a component definition (i.e. a lambda that can be passed to Tegral DI's `put`).
     */
    fun build(): ScopedContext.() -> FundefFunctionWrapper<R> {
        return {
            FundefFunctionWrapper(scope, function, qualifiers)
        }
    }
}

/**
 * Configure a fundef by providing additional qualifiers for the underlying function's parameters. Here's an example of
 * what that looks like:
 *
 * ```kotlin
 * fun myFundef(argOne: SomeComponent) {
 *     // ...
 * }
 *
 * class SomeComponent {
 *     // ...
 * }
 *
 * val env = tegralDi {
 *     put(named("ONE!!!"), ::SomeComponent)
 *     put(
 *         ::myFundef.configureFundef {
 *             "argOne" qualifyWith named("ONE!!!")
 *         }
 *     )
 * }
 * ```
 */
@ExperimentalFundef
fun <R> KFunction<R>.configureFundef(block: FundefConfigureDsl<R>.() -> Unit): FundefConfigureDsl<R> {
    return FundefConfigureDsl(this).apply(block)
}
