package guru.zoroark.tegral.di.environment

import guru.zoroark.tegral.di.FailedToResolveException
import kotlin.reflect.KClass

interface IdentifierResolver<T : Any> {
    fun resolve(components: EnvironmentComponents): T
}

abstract class CanonicalIdentifierResolver<T : Any> : IdentifierResolver<T> {
    abstract val actualClass: KClass<out T>
}

class SimpleIdentifierResolver<T : Any>(private val instance: T) : CanonicalIdentifierResolver<T>() {
    override val actualClass: KClass<out T> = instance::class

    override fun resolve(components: EnvironmentComponents): T {
        return instance
    }
}

class AliasIdentifierResolver<T : Any>(private val actualIdentifier: Identifier<out T>) : IdentifierResolver<T> {
    override fun resolve(components: EnvironmentComponents): T {
        @Suppress("UNCHECKED_CAST") // TODO provide a hand-made check for this?
        return (components[actualIdentifier] as IdentifierResolver<T>?)?.resolve(components)
            ?: throw FailedToResolveException("Failed to resolve $actualIdentifier against environment. Please report this.")
    }
}
