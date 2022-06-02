package guru.zoroark.tegral.di

import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EmptyQualifier
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.Qualifier
import guru.zoroark.tegral.di.environment.ScopedSupplier

inline fun <reified T : Any> entryOf(qualifier: Qualifier = EmptyQualifier, noinline supplier: ScopedSupplier<T>) =
    Declaration(Identifier(T::class, qualifier), supplier).let {
        it.identifier to it
    }
