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

import guru.zoroark.tegral.di.ComponentNotFoundException
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EnvironmentComponents
import guru.zoroark.tegral.di.environment.EnvironmentContext
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.IdentifierResolver
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionEnvironmentKind
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.Injector
import guru.zoroark.tegral.di.environment.MetalessInjectionScope
import guru.zoroark.tegral.di.environment.ResolvableDeclaration
import guru.zoroark.tegral.di.environment.ScopedContext
import guru.zoroark.tegral.di.environment.ScopedSupplierDeclaration
import guru.zoroark.tegral.di.environment.SimpleEnvironmentBasedScope
import guru.zoroark.tegral.di.environment.SimpleIdentifierResolver
import guru.zoroark.tegral.di.environment.ensureInstance
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.ExtensibleEnvironmentContext
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironmentKind
import guru.zoroark.tegral.di.extensions.createMetaEnvironment
import kotlin.reflect.KProperty

/**
 * A fully mutable extensible environment implementation with very little safety.
 *
 * This class **should not be used in main code** and only within test code that typically requires more flexibility
 * and the ability to add elements on the fly.
 *
 * You should use the class within a [TegralDiBaseTest], although it is not a requirement.
 *
 * ### Mutability
 *
 * You can mutate this environment (i.e. add components) by using any [put] function you are already used to. You can
 * `put` components and modules.
 *
 * You can also mutate this environment's meta-environment, either by:
 *
 * - using the [`meta { }`][meta] DSL like you would in a `tegralDi { }` block
 * - retrieving the meta-environment with [metaEnvironment]
 *
 * ### Characteristics
 *
 * - **Eager object creation**. Objects are created upon construction of this environment.
 * - **Active object injection**. Objects are re-injected at every use.
 * - **NI/Mutable**. Objects can be replaced, injection methods will not always return the same thing.<
 */
class UnsafeMutableEnvironment(
    baseContext: ExtensibleEnvironmentContext
) : TestMutableInjectionEnvironment, ExtensibleContextBuilderDsl {

    override val metaEnvironment: MutableEnvironment
    private val actualEnvironment: MutableEnvironment

    init {
        this.metaEnvironment = createMetaEnvironment(baseContext) {
            MutableEnvironment(null, it)
        }
        this.actualEnvironment = MutableEnvironment(metaEnvironment, EnvironmentContext(baseContext.declarations))
    }

    override val components: EnvironmentComponents
        get() = actualEnvironment.components

    override fun getAllIdentifiers(): Sequence<Identifier<*>> =
        actualEnvironment.getAllIdentifiers()

    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? =
        actualEnvironment.getOrNull(identifier)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getResolverOrNull(identifier: Identifier<T>): IdentifierResolver<T>? =
        actualEnvironment.components[identifier]?.let { it as IdentifierResolver<T> }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> =
        actualEnvironment.createInjector(identifier, onInjection)

    override fun meta(action: ContextBuilderDsl.() -> Unit) {
        metaEnvironment.action()
    }

    override fun <T : Any> put(declaration: Declaration<T>) {
        actualEnvironment.put(declaration)
    }

    companion object : ExtensibleInjectionEnvironmentKind<UnsafeMutableEnvironment> {
        override fun build(
            context: ExtensibleEnvironmentContext,
            metaEnvironmentKind: InjectionEnvironmentKind<*>
        ): UnsafeMutableEnvironment {
            if (metaEnvironmentKind != Meta) {
                error(
                    "Cannot create UnsafeMutableEnvironment with any meta environment other than " +
                        "UnsafeMutableEnvironment.Meta"
                )
            } else {
                return UnsafeMutableEnvironment(context)
            }
        }
    }

    /**
     * Dummy environment kind for use with [UnsafeMutableEnvironment].
     *
     * UnsafeMutableEnvironment owns and manages its own meta-environment (which is of type [MutableEnvironment]). In
     * order to "opt-in" to this behavior, you will need to specify this environment as the meta-environment kind.
     */
    object Meta : InjectionEnvironmentKind<Nothing> {
        override fun build(context: EnvironmentContext): Nothing {
            error("UnsafeMutableEnvironment.Meta environments cannot be created directly.")
        }
    }

    /**
     * A simple, mutable environment that can also dispatch requests for a meta-environment to the one passed as a
     * constructor parameter.
     *
     * You should not use this class directly. Consider instead using [UnsafeMutableEnvironment], which manages two
     * instances of `MutableEnvironment` internally.
     */
    class MutableEnvironment(
        private val metaEnvironment: MutableEnvironment?,
        baseContext: EnvironmentContext
    ) : InjectionEnvironment, ContextBuilderDsl {
        private inner class UMEInjector<T : Any>(
            private val identifier: Identifier<T>,
            private val onInjection: (T) -> Unit
        ) : Injector<T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return (getOrNull(thisRef, identifier) ?: throw ComponentNotFoundException(identifier))
                    .also(onInjection)
            }
        }

        internal val components: MutableMap<Identifier<*>, IdentifierResolver<*>> = mutableMapOf()

        init {
            for ((_, decl) in baseContext.declarations) {
                put(decl)
            }
        }

        private inner class MutableEnvironmentRedirectedInjectionScope : InjectionScope {
            override val meta: MetalessInjectionScope
                get() = SimpleEnvironmentBasedScope(this@MutableEnvironment.metaEnvironment!!)

            override fun <T : Any> inject(what: Identifier<T>): Injector<T> =
                this@MutableEnvironment.createInjector(what)
        }

        private fun <T : Any> getOrNull(parent: Any?, identifier: Identifier<T>): T? =
            components[identifier]?.resolve(parent, components)?.let { ensureInstance(identifier.kclass, it) }

        override fun <T : Any> getOrNull(identifier: Identifier<T>): T? = getOrNull(null, identifier)

        override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> =
            UMEInjector(identifier, onInjection)

        override fun <T : Any> put(declaration: Declaration<T>) {
            components[declaration.identifier] = when (declaration) {
                is ScopedSupplierDeclaration<*> -> SimpleIdentifierResolver(
                    declaration.supplier(
                        ScopedContext(
                            if (metaEnvironment == null) SimpleEnvironmentBasedScope(this)
                            else MutableEnvironmentRedirectedInjectionScope()
                        )
                    )
                )
                is ResolvableDeclaration -> declaration.buildResolver()
            }
        }

        internal fun getAllIdentifiers() = components.keys.asSequence()
    }
}
