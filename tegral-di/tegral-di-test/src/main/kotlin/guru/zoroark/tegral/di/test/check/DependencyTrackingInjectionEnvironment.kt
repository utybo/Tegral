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

package guru.zoroark.tegral.di.test.check

import guru.zoroark.tegral.di.InvalidDeclarationException
import guru.zoroark.tegral.di.environment.EnvironmentContext
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionEnvironmentKind
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.Injector
import guru.zoroark.tegral.di.environment.MetalessInjectionScope
import guru.zoroark.tegral.di.environment.ResolvableDeclaration
import guru.zoroark.tegral.di.environment.ScopedContext
import guru.zoroark.tegral.di.environment.ScopedSupplierDeclaration
import guru.zoroark.tegral.di.test.NotAvailableInTestEnvironmentException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KProperty

private class FakeInjector<T : Any> : Injector<T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        throw InvalidDeclarationException(
            "An unsafe injection was performed while trying to resolve dependencies. Use the 'safeInjection' check " +
                "for more information."
        )
    }
}

/**
 * A "kind" of dependency detected by [DependencyTrackingInjectionEnvironment].
 */
enum class DependencyKind(
    /**
     * The look of the 3-characters arrow used to represent such a dependency in check messages.
     */
    val arrow: String
) {
    /**
     * An injection dependency, i.e. a component that requests another via a `by scope()` call.
     */
    Injection("-->"),

    /**
     * A resolution dependency, i.e. a resolver that uses, as part of its resolution, another component.
     */
    Resolution("R->")
}

/**
 * The dependencies for an identifier, with the kind of dependency "towards" those dependencies.
 */
data class IdentifierDependencies(
    /**
     * The kind of dependency.
     */
    val kind: DependencyKind,
    /**
     * The identifiers of the dependencies.
     */
    val dependencies: List<Identifier<*>>
)

/**
 * Fake environment that tracks dependencies on the instantiation of components.
 *
 * Environment of this kind should rarely be created manually: they are used behind the scenes in rules that check for
 * the coherence of the contents of an environment (completeness check, cyclic dependency check...)
 *
 * This environment performs dependency analysis on:
 *
 * - Components
 * - Injection resolution
 */
class DependencyTrackingInjectionEnvironment(context: EnvironmentContext) : InjectionEnvironment {
    companion object : InjectionEnvironmentKind<DependencyTrackingInjectionEnvironment> {
        override fun build(context: EnvironmentContext): DependencyTrackingInjectionEnvironment =
            DependencyTrackingInjectionEnvironment(context)
    }

    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? {
        throw NotAvailableInTestEnvironmentException("Not available on this kind of environment")
    }

    private val currentInjections = mutableListOf<Identifier<*>>()

    /**
     * The dependencies represented as a map from an identifier to the identifiers this identifier depends on.
     */
    val dependencies: Map<Identifier<*>, IdentifierDependencies> = context.declarations.mapValues { (_, v) ->
        when (v) {
            is ScopedSupplierDeclaration -> {
                currentInjections.clear()
                try {
                    v.supplier(ScopedContext(EnvironmentBasedIgnoringMetaScope(this)))
                } catch (e: InvocationTargetException) {
                    // Skips InvocationTargetException that occurs during unsafe injections (from FakeInjector)
                    throw e.cause ?: throw e
                }
                IdentifierDependencies(DependencyKind.Injection, currentInjections.toList())
            }
            is ResolvableDeclaration -> {
                IdentifierDependencies(DependencyKind.Resolution, v.buildResolver().requirements)
            }
        }
    }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> {
        currentInjections += identifier
        return FakeInjector()
    }

    override fun getAllIdentifiers(): Sequence<Identifier<*>> = dependencies.keys.asSequence()
}

internal class EnvironmentBasedIgnoringMetaScope(
    private val environment: InjectionEnvironment
) : InjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
        return environment.createInjector(what)
    }

    override val meta: MetalessInjectionScope
        get() = FakeMetalessInjectionScope()
}

private class FakeMetalessInjectionScope : MetalessInjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> = FakeInjector()
}
