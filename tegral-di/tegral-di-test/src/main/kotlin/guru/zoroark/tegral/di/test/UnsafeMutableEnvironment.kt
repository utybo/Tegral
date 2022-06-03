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

import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EnvironmentBasedScope
import guru.zoroark.tegral.di.environment.EnvironmentContext
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionEnvironmentKind
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.Injector
import guru.zoroark.tegral.di.environment.MetalessInjectionScope
import guru.zoroark.tegral.di.environment.ScopedContext
import guru.zoroark.tegral.di.environment.SimpleEnvironmentBasedScope
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
 * - **NI/Mutable**. Objects can be replaced, injection methods will not always return the same thing.
 */
class UnsafeMutableEnvironment(
    baseContext: ExtensibleEnvironmentContext
) : TestMutableInjectionEnvironment, ExtensibleContextBuilderDsl {

    override val metaEnvironment: MutableEnvironment
    val actualEnvironment: MutableEnvironment

    init {
        this.metaEnvironment = createMetaEnvironment(baseContext) {
            MutableEnvironment(null, it)
        }
        this.actualEnvironment = MutableEnvironment(metaEnvironment, EnvironmentContext(baseContext.declarations))
    }

    override val components: Map<Identifier<*>, Any>
        get() = actualEnvironment.components

    override fun getAllIdentifiers(): Sequence<Identifier<*>> =
        actualEnvironment.getAllIdentifiers()

    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? =
        actualEnvironment.getOrNull(identifier)

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

    object Meta : InjectionEnvironmentKind<Nothing> {
        override fun build(context: EnvironmentContext): Nothing {
            error("UnsafeMutableEnvironment.Meta environments cannot be created directly.")
        }
    }

    class MutableEnvironment(
        private val metaEnvironment: MutableEnvironment?,
        baseContext: EnvironmentContext
    ) : InjectionEnvironment, ContextBuilderDsl {
        private inner class UMEInjector<T : Any>(
            private val identifier: Identifier<T>,
            private val onInjection: (T) -> Unit
        ) : Injector<T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return get(identifier).also(onInjection)
            }
        }

        val components = baseContext.declarations.mapValues { (_, decl) ->
            decl.supplier(
                ScopedContext(
                    if (metaEnvironment == null) SimpleEnvironmentBasedScope(this)
                    else object : InjectionScope {
                        override val meta: MetalessInjectionScope
                            get() = SimpleEnvironmentBasedScope(metaEnvironment)

                        override fun <T : Any> inject(what: Identifier<T>): Injector<T> =
                            this@MutableEnvironment.createInjector(what)
                    }
                )
            )
        }.toMutableMap()

        override fun <T : Any> getOrNull(identifier: Identifier<T>): T? =
            components[identifier]?.let { ensureInstance(identifier.kclass, it) }

        override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> =
            UMEInjector(identifier, onInjection)

        override fun <T : Any> put(declaration: Declaration<T>) {
            components[declaration.identifier] =
                declaration.supplier(ScopedContext(EnvironmentBasedScope(this)))
        }

        fun getAllIdentifiers() = components.keys.asSequence()
    }
}
