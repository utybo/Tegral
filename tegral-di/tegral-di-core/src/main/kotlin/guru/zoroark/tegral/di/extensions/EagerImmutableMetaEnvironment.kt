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

import guru.zoroark.tegral.di.ComponentNotFoundException
import guru.zoroark.tegral.di.InternalErrorException
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.Declarations
import guru.zoroark.tegral.di.environment.EnvironmentBasedScope
import guru.zoroark.tegral.di.environment.EnvironmentComponents
import guru.zoroark.tegral.di.environment.EnvironmentContext
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.IdentifierResolver
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionEnvironmentKind
import guru.zoroark.tegral.di.environment.Injector
import guru.zoroark.tegral.di.environment.ResolvableDeclaration
import guru.zoroark.tegral.di.environment.ScopedContext
import guru.zoroark.tegral.di.environment.ScopedSupplierDeclaration
import guru.zoroark.tegral.di.environment.SimpleIdentifierResolver
import guru.zoroark.tegral.di.environment.ensureInstance
import kotlin.reflect.KProperty

private class StaticInjector<T : Any>(private val value: T) : Injector<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }
}

/**
 * An injection environment implementation with an eager evaluation strategy.
 *
 * Note that this environment is only intended for *meta-environments*. It is not intended for regular-purpose
 * (extensible) environments, which should instead use a
 * [MixedImmutableEnvironment][guru.zoroark.tegral.di.environment.MixedImmutableEnvironment].
 *
 * ### Characteristics
 *
 * - **Eager object creation**. Objects are created upon construction of this environment.
 * - **Eager object injection**. Objects are injected upon calling the injection method.
 * - **Idempotent/Immutable**. Objects cannot be replaced, injection methods will always return the same thing.
 */
class EagerImmutableMetaEnvironment(context: EnvironmentContext) : InjectionEnvironment {
    companion object : InjectionEnvironmentKind<EagerImmutableMetaEnvironment> {
        override fun build(context: EnvironmentContext): EagerImmutableMetaEnvironment {
            return EagerImmutableMetaEnvironment(context)
        }
    }

    private val components = initializeComponents(context)
    private var buildingInformation: EIEBeingBuiltInformation? = null

    private fun initializeComponents(context: EnvironmentContext): EnvironmentComponents {
        val componentsNow = mutableMapOf<Identifier<*>, IdentifierResolver<*>>()
        val bi = EIEBeingBuiltInformation(context.declarations, componentsNow)
        buildingInformation = bi

        // Preparation phase
        for ((_, declaration) in context.declarations) {
            initializeComponentResolver(declaration)
        }

        // Resolution phase
        for ((parent, childInjectors) in bi.injectors) {
            // Note that "parent" will always be a simple resolver (i.e. just mapped to an instance). The actual
            // requester does not matter during its resolution.
            val parentInstance = bi.componentsBeingBuilt[parent]!!.resolve(null, bi.componentsBeingBuilt)
            for (childInjector in childInjectors) {
                childInjector.resolve(parentInstance, bi.componentsBeingBuilt)
            }
        }

        buildingInformation = null
        return componentsNow.toMap()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? {
        return components[identifier]?.resolve(null, components)?.also { ensureInstance(identifier.kclass, it) } as T?
    }

    private data class EIEBeingBuiltInformation(
        val declarations: Declarations,
        val componentsBeingBuilt: MutableMap<Identifier<*>, IdentifierResolver<*>>,
        val injectors: MutableMap<Identifier<*>, List<TwoPhaseInjector<*>>> = mutableMapOf(),
        var injectorsOfCurrentInstance: MutableList<TwoPhaseInjector<*>>? = null
    )

    private class TwoPhaseInjector<T : Any>(
        resolver: IdentifierResolver<T>
    ) : Injector<T> {
        private sealed class State<T : Any>
        private class Prepared<T : Any>(val resolver: IdentifierResolver<T>) : State<T>()
        private class Resolved<T : Any>(val instance: T) : State<T>()

        var state: State<T> = Prepared(resolver)

        fun resolve(requester: Any, components: EnvironmentComponents) {
            val previousState = state
            if (previousState !is Prepared) {
                throw InternalErrorException("Attempted to resolve an injector that was already resolved.")
            }

            state = Resolved(previousState.resolver.resolve(requester, components))
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            val actualState = state
            if (actualState !is Resolved) {
                throw InternalErrorException("Attempted to get a value from an injector that was not resolved.")
            }
            return actualState.instance
        }
    }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> {
        val info = buildingInformation
        return if (info != null) {
            if (info.injectorsOfCurrentInstance == null) {
                throw InternalErrorException(
                    "createInjector called during build phase without an actual parent being created. " +
                        "Please report this."
                )
            }
            val injector = initializeComponentResolver(
                (info.declarations[identifier] ?: throw ComponentNotFoundException(identifier)) as Declaration<T>
            )
            info.injectorsOfCurrentInstance!!.add(injector)
            injector
        } else {
            val value = getOrNull(identifier) ?: throw ComponentNotFoundException(identifier)
            onInjection(value)
            StaticInjector(value)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> initializeComponentResolver(
        declaration: Declaration<T>
    ): TwoPhaseInjector<T> {
        val bi = buildingInformation ?: error("initializeComponentResolver called outside of building phase")
        // Initialize the component's resolver *or* grab it from the components map.
        // In case the resolver is for an actual instance, this is where the instantiation is done.
        val resolver = bi.componentsBeingBuilt.getOrPut(declaration.identifier) {
            when (declaration) {
                is ScopedSupplierDeclaration<T> -> {
                    val prevInjectors = bi.injectorsOfCurrentInstance

                    bi.injectorsOfCurrentInstance = mutableListOf()
                    val resolver =
                        SimpleIdentifierResolver(declaration.supplier(ScopedContext(EnvironmentBasedScope(this))))
                    bi.injectors[declaration.identifier] = bi.injectorsOfCurrentInstance!!.toList()

                    bi.injectorsOfCurrentInstance = prevInjectors
                    resolver
                }
                is ResolvableDeclaration<T> -> declaration.buildResolver()
            }
        } as IdentifierResolver<T>
        return TwoPhaseInjector(resolver)
    }
}
