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

import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.resolvers.CanonicalIdentifierResolver
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * Filters the given identifiers to only include those whose actual object are subclasses of the given class parameter.
 *
 * This class properly handles:
 *
 * - Cases where the "advertised" type in the identifier is not a subclass but the actual instance is (usually the case
 *   with services).
 * - Ignore non-canonical resolvers (e.g. this function only keeps actual objects and will not include aliases that
 *   happen to also match the subclass).
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> Sequence<Identifier<*>>.filterSubclassesOf(
    environment: ExtensibleInjectionEnvironment,
    kclass: KClass<T>
): Sequence<Identifier<T>> =
    filter {
        // KClass' isSubclassOf seems to be broken when using proxies (e.g. mocks), so we use Java's Class equivalents
        // instead.
        (environment.getResolverOrNull(it) as? CanonicalIdentifierResolver<*>)
            ?.actualClass?.java?.isSubclassOf(kclass) ?: false
    } as Sequence<Identifier<T>>

private fun Class<*>.isSubclassOf(other: KClass<*>): Boolean = other.java.isAssignableFrom(this)
