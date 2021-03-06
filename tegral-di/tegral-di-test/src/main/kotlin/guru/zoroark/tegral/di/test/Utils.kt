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

package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EmptyQualifier
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.Qualifier
import guru.zoroark.tegral.di.environment.ScopedSupplier
import guru.zoroark.tegral.di.environment.ScopedSupplierDeclaration

/**
 * Creates a [Declaration], automatically creating the identifier from [T] and [qualifier] and using the given
 * [supplier]. Returns a pair with the identifier and the declaration.
 */
inline fun <reified T : Any> entryOf(qualifier: Qualifier = EmptyQualifier, noinline supplier: ScopedSupplier<T>) =
    ScopedSupplierDeclaration(Identifier(T::class, qualifier), supplier).let {
        it.identifier to it
    }

/**
 * Creates an entry directly from a declaration instead of from a qualifier + supplier.
 */
fun <T : Any> entryOf(declaration: Declaration<T>) = declaration.identifier to declaration
