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

package guru.zoroark.tegral.di.dsl

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.InvalidDeclarationException
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EmptyQualifier
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.Qualifier
import guru.zoroark.tegral.di.environment.ScopedSupplier
import guru.zoroark.tegral.di.environment.ScopedSupplierDeclaration
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.createType

/**
 * Context for Tegral DI builders, such as module builders ([tegralDiModule]) or environment builders ([tegralDi]).
 *
 * DSL elements should use this class as a receiver, like the [put] functions.
 */
@TegralDsl
interface ContextBuilderDsl {
    /**
     * Adds a declaration to this context. You will rarely need to call this function directly, as many other [put]
     * functions exist for a more DSL-ish approach.
     */
    @TegralDsl
    fun <T : Any> put(declaration: Declaration<T>)
}

/**
 * Add a definition of type [T] with the given supplier.
 *
 * [T] is internally turned to a KClass.
 *
 * @param supplier The lambda that will be executed to create an object.
 * @param T The type of the component to add.
 * @returns The created declaration.
 */
@TegralDsl
inline fun <reified T : Any> ContextBuilderDsl.put(noinline supplier: ScopedSupplier<T>) =
    put(T::class, EmptyQualifier, supplier)

/**
 * Add a definition of type [T] with the given qualifier and supplier.
 *
 * [T] is internally turned to a KClass. Qualifiers allow for differentiating between components that have the same
 * type. See [Identifier] for more information.
 *
 * @param supplier The lambda that will be executed to create an object.
 * @param qualifier The qualifier for the underlying [identifier][Identifier].
 * @param T The type of the component to add.
 * @returns The created declaration.
 */
@TegralDsl
inline fun <reified T : Any> ContextBuilderDsl.put(qualifier: Qualifier, noinline supplier: ScopedSupplier<T>) =
    put(T::class, qualifier, supplier)

/**
 * Add a definition of type [T] with the given qualifier and supplier.
 *
 * [T] is internally turned to a KClass. Qualifiers allow for differentiating between components that have the same
 * type. See [Identifier] for more information.
 *
 * @param supplier The lambda that will be executed to create an object.
 * @param qualifier The qualifier for the underlying [identifier][Identifier].
 * @param T The type of the component to add.
 * @returns The created declaration.
 */
@TegralDsl
fun <T : Any> ContextBuilderDsl.put(
    kclass: KClass<T>,
    qualifier: Qualifier = EmptyQualifier,
    supplier: ScopedSupplier<T>
) = ScopedSupplierDeclaration(Identifier(kclass, qualifier), supplier)
    .also { put(it) }

/**
 * Add a definition of type [T] with the given constructor.
 *
 * [T] is internally turned to a KClass.
 *
 * @param supplier The constructor for creating an object. This can be passed as a reference, e.g. `::MyClass`.
 * @param T The type of the component to add.
 * @returns The created declaration.
 */
@TegralDsl
inline fun <reified T : Any> ContextBuilderDsl.put(supplier: KFunction<T>) =
    put(T::class, EmptyQualifier, supplier)

/**
 * Add a definition of type [T] with the given qualifier and constructor.
 *
 * [T] is internally turned to a KClass. Qualifiers allow for differentiating between components that have the same
 * type. See [Identifier] for more information.
 *
 *
 * @param supplier The constructor for creating an object. This can be passed as a reference, e.g. `::MyClass`.
 * @param qualifier The qualifier for the type.
 * @param T The type of the component to add.
 * @returns The created declaration.
 */
@TegralDsl
inline fun <reified T : Any> ContextBuilderDsl.put(qualifier: Qualifier, supplier: KFunction<T>) =
    put(T::class, qualifier, supplier)

/**
 * Add a definition of type [T] with the given class and constructor.
 *
 * @param kclass The class of the type [T]. This is useful for scenarios where it is not known at compile time. If the
 * type is already known at compile time, you can remove the `kclass` parameter.
 * @param supplier The constructor for creating an object. This can be passed as a reference, e.g. `::MyClass`.
 * @param T The type of the component to add.
 * @returns The created declaration.
 */
@TegralDsl
fun <T : Any> ContextBuilderDsl.put(kclass: KClass<T>, supplier: KFunction<T>) =
    put(kclass, EmptyQualifier, supplier)

/**
 * Add a definition of type [T] with the given class, qualifier and constructor.
 *
 * Qualifiers allow for differentiating between components that have the same  type. See [Identifier] for more
 * information.
 *
 * @param kclass The class of the type [T]. This is useful for scenarios where it is not known at compile time. If the
 * type is already known at compile time, you can remove the `kclass` parameter.
 * @param qualifier The qualifier for the type.
 * @param supplier The constructor for creating an object. This can be passed as a reference, e.g. `::MyClass`.
 * @param T The type of the component to add.
 * @returns The created declaration.
 */
@TegralDsl
fun <T : Any> ContextBuilderDsl.put(kclass: KClass<T>, qualifier: Qualifier, supplier: KFunction<T>) =
    when {
        supplier.returnType.isMarkedNullable ->
            throw InvalidDeclarationException("Cannot 'put' a function that has a nullable return type.")
        supplier.parameters.isEmpty() ->
            put(kclass, qualifier) { supplier.call() }
        // Requires a scope
        supplier.parameters.size == 1 && supplier.parameters.first().type == InjectionScope::class.createType() ->
            put(kclass, qualifier) { supplier.call(scope) }
        else -> throw InvalidDeclarationException(
            "Cannot 'put' the given function ($supplier). It must take either no arguments or a single argument " +
                "that is of type 'InjectionScope'. Consider manually instantiating this component instead."
        )
    }

/**
 * Add a module to the current definition. This adds all the declarations within the module to this context.
 *
 * @param module The module that should be added.
 */
@TegralDsl
fun ContextBuilderDsl.put(module: InjectableModule) {
    module.declarations.forEach { put(it) }
}
