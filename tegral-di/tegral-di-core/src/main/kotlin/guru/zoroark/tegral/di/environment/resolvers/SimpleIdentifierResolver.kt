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
import kotlin.reflect.KClass

/**
 * A simple resolver that maps to an object instance. This is a [canonical][CanonicalIdentifierResolver] resolver.
 */
class SimpleIdentifierResolver<T : Any>(private val instance: T) : CanonicalIdentifierResolver<T>() {
    override val actualClass: KClass<out T> = instance::class
    override val requirements: List<Identifier<*>> = emptyList()

    override fun resolve(requester: Any?, components: EnvironmentComponents): T {
        return instance
    }
}
