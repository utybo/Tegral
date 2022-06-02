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

package guru.zoroark.tegral.di.environment

import guru.zoroark.tegral.di.NotExtensibleException
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment

/**
 * An [injection scope][InjectionScope] that delegates the injection to the given environment, without any support for
 * injecting from meta-environments.
 *
 * Use the [EnvironmentBasedScope] constructor function to create the appropriate instance.
 */
class SimpleEnvironmentBasedScope(private val env: InjectionEnvironment) : InjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
        return env.createInjector(what)
    }

    override val meta
        get() = throw NotExtensibleException("This environment does not have a meta-environment")
}

/**
 * An [injection scope][InjectionScope] that delegates the injection to the given environment (for `inject`) or its
 * meta-environment (for `meta`).
 *
 * Use the [EnvironmentBasedScope] constructor function to create the appropriate instance.
 */
class ExtensibleEnvironmentBasedScope(private val env: ExtensibleInjectionEnvironment) : InjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
        return env.createInjector(what)
    }

    override val meta: MetalessInjectionScope = SimpleEnvironmentBasedScope(env.metaEnvironment)
}

/**
 * Creates an injection scope based on the given environment. If the given environment supports meta-environments (i.e.
 * if it is extensible), an ExtensibleEnvironmentBasedScope will be returned. Otherwise, a SimpleEnvironmentBasedScope
 * is used.
 *
 * This should be used in almost all cases - the ability to control the `inject` call mechanism is mostly useful for
 * dependencies analysis.
 */
@Suppress("FunctionNaming", "FunctionName")
fun EnvironmentBasedScope(env: InjectionEnvironment): InjectionScope =
    if (env is ExtensibleInjectionEnvironment) ExtensibleEnvironmentBasedScope(env)
    else SimpleEnvironmentBasedScope(env)
