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

package guru.zoroark.tegral.di.environment.resolvers

import guru.zoroark.tegral.di.environment.EnvironmentComponents
import guru.zoroark.tegral.di.environment.Identifier

/**
 * A resolver is an object that helps resolve an identifier into an actual component.
 *
 * The implementation dictates how such a component is retrieved, created, etc.
 */
interface IdentifierResolver<T : Any> {
    /**
     * Resolve using the given components. The components may be used in case the resolver requires more complicated
     * use cases.
     *
     * @param requester The component requesting the resolution in the case of injections *or* null if the resolution
     * was caused manually (e.g. by calling `get` on an environment).
     */
    fun resolve(requester: Any?, components: EnvironmentComponents): T

    /**
     * The requirements for this resolver, i.e. the identifiers that this resolver needs from the `components` argument
     * of [resolve]. This information is used for dependency analysis to properly "link" components that are resolved
     * against during resolution, such as with aliases.
     *
     * It is valid (and expected) to return an empty list here in case you do not need the `components` argument at all.
     */
    val requirements: List<Identifier<*>>
}
