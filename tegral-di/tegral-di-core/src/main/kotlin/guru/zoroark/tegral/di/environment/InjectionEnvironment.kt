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

/**
 * An injection environment is, in a nutshell, a container for injectable components. These injectable components can be
 * retrieved in two ways:
 *
 * - Via the [get] function, which retrieves the component identified according to the given parameter
 * - Via the [createInjector] function, which returns an [Injector] that is able to retrieve injectable elements using a
 * Kotlin property delegator.
 *
 * ### Guarantees (or lack thereof)
 *
 * Internally, injection environments can use whatever mechanism they want. No guarantees are given on the mutability or
 * stability of the returned components -- it is up to the implementation of the injection environment to provide such
 * guarantees.
 *
 * All implementations should clearly state their characteristics in their documentation:
 *
 * - **Lazy** or **eager object creation**
 * - **Lazy**, **eager** or **active object injection**.
 * - **Idempotent/Immutable**, **NI/Immutable** or **NI/Mutable** (NI = non-idempotent).
 *
 * Here are the templates that should be used:
 *
 * - **Object creation**
 *      - **Eager object creation**. Objects are created upon construction of this environment.
 *      - **Lazy object creation**. Objects are created upon first use.
 * - **Object injection**
 *      - **Eager object injection**. Objects are injected upon calling the injection method.
 *      - **Lazy object injection**. Objects are injected upon first use, and are only computed once.
 *      - **Active object injection**. Objects are re-injected at every use.
 * - **Mutability**
 *      - **Idempotent/Immutable**. Objects cannot be replaced, injection methods will always return the same thing.
 *      - **NI/Immutable**. Objects cannot be replaced, injection methods will not always return the same thing.
 *      - **NI/Mutable**. Objects can be replaced, injection methods will not always return the same thing.
 *
 * ### Companion object
 *
 * The bridge between the DSL and environments is made via a [InjectionEnvironmentKind] object. This object can be
 * passed to the [guru.zoroark.tegral.di.dsl.tegralDi] function to determine the environment that should be built.
 *
 * Implementors should have a companion object that implements [InjectionEnvironmentKind], allowing for easy usage
 * within the DSL. For example:
 *
 * ```kotlin
 * class MyInjectionEnvironment : InjectionEnvironment {
 *     companion object : InjectionEnvironmentKind {
 *         fun build(context: EnvironmentContext): MyInjectionEnvironment {
 *             // ...
 *         }
 *     }
 *     // ...
 * }
 *
 * tegralDi(MyInjectionEnvironment) {
 *     // ...
 * }
 * ```
 */
interface InjectionEnvironment {
    /**
     * Gets the component identified by the given identifier. No guarantees are given on this function - it may not be
     * idempotent, depending on the actual implementation.
     */
    fun <T : Any> get(identifier: Identifier<T>): T =
        getOrNull(identifier) ?: throw ComponentNotFoundException(identifier)

    /**
     * Gets the component identified by the given identifier, or null if no such component exists. No guarantees are
     * given on this function - it may not be idempotent, depending on the actual implementation.
     */
    fun <T : Any> getOrNull(identifier: Identifier<T>): T?

    /**
     * Creates an [Injector] that can be used as a property delegator, bound against the given identifier.
     *
     * @param T The injected component's type
     * @param identifier The identifier to create an injector for
     * @param onInjection Callback that must be called whenever the injection occurs. This is used for debugging and
     * testing purposes. Note that an *injection* only happens when the environment is actually queried for an object.
     * Because of this, eager and lazy injection only actually perform the injection once, while active injection always
     * performs an injection.
     */
    fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit = {}): Injector<T>
}

/**
 * Gets the component identified by the given type turned into an [Identifier] with an optional [qualifier][Qualifier].
 * Throws an exception if no component with this identifier exists.
 *
 * Refer to [InjectionEnvironment.get] for more information.
 */
inline fun <reified T : Any> InjectionEnvironment.get(qualifier: Qualifier = EmptyQualifier): T =
    get(Identifier(T::class, qualifier))

/**
 * Gets the component identified by the given type turned into an [Identifier] with an optional [qualifier][Qualifier].
 * Returns null if no component with this identifier exists.
 *
 * Refer to [InjectionEnvironment.getOrNull] for more information.
 */
inline fun <reified T : Any> InjectionEnvironment.getOrNull(qualifier: Qualifier = EmptyQualifier): T? =
    getOrNull(Identifier(T::class, qualifier))
