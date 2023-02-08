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

import guru.zoroark.tegral.di.environment.Qualifier
import kotlin.reflect.KFunction

/**
 * Qualifier for components that depend on a function. This is mainly for use with fundefs with components of type
 * [FundefFunctionWrapper]
 *
 * @property function The function this qualifier depends on
 * @param R The return type of the underlying function
 */
@ExperimentalFundef
data class FunctionQualifier<R>(val function: KFunction<R>) : Qualifier

/**
 * Creator DSL-ish function for [FunctionQualifier].
 */
@ExperimentalFundef
fun <R> ofFunction(function: KFunction<R>): Qualifier = FunctionQualifier(function)
