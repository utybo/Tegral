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

import guru.zoroark.tegral.di.FailedToResolveException
import guru.zoroark.tegral.di.environment.EnvironmentComponents
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.ensureInstance

/**
 * A resolver that is aliased to another identifier. Not canonical.
 */
class AliasIdentifierResolver<T : Any>(private val actualIdentifier: Identifier<out T>) : IdentifierResolver<T> {
    override val requirements: List<Identifier<*>> = listOf(actualIdentifier)

    override fun resolve(requester: Any?, components: EnvironmentComponents): T {
        val actualResolver = components[actualIdentifier] ?: throw FailedToResolveException(
            "Failed to resolve $actualIdentifier against environment. Make sure that what your alias is pointing " +
                "to ($actualIdentifier) actually exists in the environment."
        )
        val actualInstance = actualResolver.resolve(requester, components)
        return ensureInstance(actualIdentifier.kclass, actualInstance)
    }
}
