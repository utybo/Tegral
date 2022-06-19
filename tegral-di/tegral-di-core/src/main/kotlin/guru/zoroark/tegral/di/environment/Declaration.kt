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

import guru.zoroark.tegral.di.ComponentNotFoundException
import guru.zoroark.tegral.di.extensions.DeclarationTag


sealed class Declaration<T : Any>(val identifier: Identifier<T>) {
    /**
     * Tags attached to this declaration.
     */
    val tags = mutableListOf<DeclarationTag>()
}

/**
 * A declaration within an [EnvironmentContext].
 *
 * A declaration is an [Identifier] coupled with a [scoped supplier][ScopedSupplier]. Declarations can be eagerly or
 * lazily invoked depending on the underlying environment's general contract.
 *
 * @property identifier The identifier for this declaration.
 * @property supplier The supplier for this declaration.
 */
class ScopedSupplierDeclaration<T : Any>(identifier: Identifier<T>, val supplier: ScopedSupplier<T>) :
    Declaration<T>(identifier)

abstract class ResolvableDeclaration<T : Any>(identifier: Identifier<T>) : Declaration<T>(identifier) {
    abstract fun buildResolver(): IdentifierResolver<T>
}

/**
 * A map that maps identifiers to declarations.
 *
 * The general contract is:
 *
 * - For any key-value pair, key == value.identifier
 * - For any key-value pair, key: Identifier of T => value: Declaration of T
 */
typealias Declarations = Map<Identifier<*>, Declaration<*>>

/**
 * Retrieves the declaration that corresponds to the given type parameter. This function does *not* check for the
 * validity or coherence of the returned declaration.
 */
inline fun <reified T : Any> Declarations.get(): Declaration<T> {
    val identifier = Identifier(T::class)
    val found = this[identifier] ?: throw ComponentNotFoundException(identifier)
    @Suppress("UNCHECKED_CAST")
    return found as Declaration<T>
}
