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

package guru.zoroark.tegral.di.extensions

import guru.zoroark.tegral.di.environment.InjectionEnvironmentKind

/**
 * Equivalent of [InjectionEnvironmentKind] for environments that support installable extensions.
 *
 * Should be implemented in the companion object of extensible injection environments.
 *
 * See [ExtensibleInjectionEnvironment] for more details.
 */
interface ExtensibleInjectionEnvironmentKind<E : ExtensibleInjectionEnvironment> {
    /**
     * Builds an extensible environment of type [E] using the given context and given meta-environment kind.
     */
    fun build(
        context: ExtensibleEnvironmentContext,
        metaEnvironmentKind: InjectionEnvironmentKind<*> = EagerImmutableMetaEnvironment
    ): E
}
