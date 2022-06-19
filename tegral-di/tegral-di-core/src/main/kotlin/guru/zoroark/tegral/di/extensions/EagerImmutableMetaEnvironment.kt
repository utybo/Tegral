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

private data class EIEBeingBuiltInformation(
    val declarations: Declarations,
    val componentsBeingBuilt: MutableMap<Identifier<*>, IdentifierResolver<*>>
)

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
        buildingInformation = EIEBeingBuiltInformation(context.declarations, componentsNow)

        for ((_, declaration) in context.declarations) {
            initializeComponent(componentsNow, declaration)
        }
        buildingInformation = null
        return componentsNow.toMap()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? {
        return components[identifier]?.resolve(components)?.also { ensureInstance(identifier.kclass, it) } as T?
    }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> {
        val info = buildingInformation
        if (info != null) {
            @Suppress("UNCHECKED_CAST")
            return StaticInjector(
                initializeComponent(
                    info.componentsBeingBuilt,
                    (info.declarations[identifier] ?: throw ComponentNotFoundException(identifier)) as Declaration<T>
                ).also(onInjection)
            )
        } else {
            val value = getOrNull(identifier) ?: throw ComponentNotFoundException(identifier)
            onInjection(value)
            return StaticInjector(value)
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> EagerImmutableMetaEnvironment.initializeComponent(
    components: MutableMap<Identifier<*>, IdentifierResolver<*>>,
    declaration: Declaration<T>
): T {
    return components.getOrPut(declaration.identifier) {
        when (declaration) {
            is ScopedSupplierDeclaration<T> ->
                SimpleIdentifierResolver(declaration.supplier(ScopedContext(EnvironmentBasedScope(this))))
            is ResolvableDeclaration<T> -> declaration.buildResolver()
        }
    }.resolve(components) as T
}
