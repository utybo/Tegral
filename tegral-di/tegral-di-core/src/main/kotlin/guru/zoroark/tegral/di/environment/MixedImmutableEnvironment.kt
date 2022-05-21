package guru.zoroark.tegral.di.environment

import guru.zoroark.tegral.di.ComponentNotFoundException
import guru.zoroark.tegral.di.extensions.DefaultExtensibleInjectionEnvironment
import guru.zoroark.tegral.di.extensions.EagerImmutableMetaEnvironment
import guru.zoroark.tegral.di.extensions.ExtensibleEnvironmentContext
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironmentKind
import kotlin.reflect.KProperty

/**
 * An injection environment implementation with a *mixed evaluation strategy*.
 *
 * Mixed evaluation strategy here means that:
 * - Components are created eagerly, i.e. when this environment is created
 * - Injections are performed lazily within the same component, e.g. a component A that wants to have B injected will
 *   only actually get B when A performs a `get` on B.
 *
 * Mixed evaluation allows for lock-free thread safety for component creation and occasional locking thread safety on
 * injections.
 *
 * Due to injections being made lazily, this environment supports cyclic dependencies (class A requires B, class B
 * requires A) and self-injections (class C requires itself).
 *
 * ### Characteristics
 *
 * - **Eager object creation**. Objects are created upon construction of this environment.
 * - **Lazy object injection**. Objects are injected upon first use, and are only computed once.
 * - **Idempotent/Immutable**. Objects cannot be replaced, injection methods will always return the same thing.
 *
 * Compatible with installable extensions.
 */
class MixedImmutableEnvironment(
    context: ExtensibleEnvironmentContext,
    metaEnvironmentKind: InjectionEnvironmentKind<*> = EagerImmutableMetaEnvironment
) : DefaultExtensibleInjectionEnvironment(context, metaEnvironmentKind) {

    companion object : ExtensibleInjectionEnvironmentKind<MixedImmutableEnvironment> {
        override fun build(context: ExtensibleEnvironmentContext, metaEnvironmentKind: InjectionEnvironmentKind<*>) =
            MixedImmutableEnvironment(context, metaEnvironmentKind)
    }

    private inner class MIEInjector<T : Any>(
        private val identifier: Identifier<T>,
        private val onInjection: (T) -> Unit
    ) : Injector<T> {
        private val value by lazy {
            val result = components[identifier] ?: throw ComponentNotFoundException(identifier)
            ensureInstance(identifier.kclass, result).also(onInjection)
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T = value
    }

    private val components = context.declarations.mapValues { (_, decl) ->
        decl.supplier(ScopedContext(EnvironmentBasedScope(this)))
    }

    override fun getAllIdentifiers(): Sequence<Identifier<*>> = components.keys.asSequence()

    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? =
        components[identifier]?.let { ensureInstance(identifier.kclass, it) }

    override fun <T : Any> createInjector(
        identifier: Identifier<T>,
        onInjection: (T) -> Unit
    ): Injector<T> =
        MIEInjector(identifier, onInjection)
}
