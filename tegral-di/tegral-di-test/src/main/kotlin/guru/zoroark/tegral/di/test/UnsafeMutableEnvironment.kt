package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EnvironmentBasedScope
import guru.zoroark.tegral.di.environment.EnvironmentContext
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionEnvironmentKind
import guru.zoroark.tegral.di.environment.Injector
import guru.zoroark.tegral.di.environment.ScopedContext
import guru.zoroark.tegral.di.environment.ensureInstance
import kotlin.reflect.KProperty

/**
 * A fully mutable environment implementation with very little safety.
 *
 * This class **should not be used in main code** and only within test code that typically requires more flexibility
 * and the ability to add elements on the fly.
 *
 * You should use the class within a [TegralDiBaseTest], although it is not a requirement.
 *
 * ### Mutability
 *
 * You can mutate this environment (i.e. add components) by using any [put] function you are already used to. You can
 * `put` components and modules. You can also use [alsoPut] to add them in a suffix-style.
 *
 * ### Characteristics
 *
 * - **Eager object creation**. Objects are created upon construction of this environment.
 * - **Active object injection**. Objects are re-injected at every use.
 * - **NI/Mutable**. Objects can be replaced, injection methods will not always return the same thing.
 */
class UnsafeMutableEnvironment(baseContext: EnvironmentContext) : TestMutableInjectionEnvironment, ContextBuilderDsl {
    companion object : InjectionEnvironmentKind<UnsafeMutableEnvironment> {
        override fun build(context: EnvironmentContext) = UnsafeMutableEnvironment(context)
    }

    private inner class UMEInjector<T : Any>(
        private val identifier: Identifier<T>,
        private val onInjection: (T) -> Unit
    ) : Injector<T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return get(identifier).also(onInjection)
        }
    }

    /**
     * The internal components map used by this environment. You can use it to directly manipulate and retrieve
     * components stored in this environment.
     */
    override val components = baseContext.declarations.mapValues { (_, decl) ->
        decl.supplier(ScopedContext(EnvironmentBasedScope(this)))
    }.toMutableMap()

    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? =
        components[identifier]?.let { ensureInstance(identifier.kclass, it) }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> =
        UMEInjector(identifier, onInjection)

    override fun <T : Any> put(declaration: Declaration<T>) {
        components[declaration.identifier] =
            declaration.supplier(ScopedContext(EnvironmentBasedScope(this)))
    }
}
