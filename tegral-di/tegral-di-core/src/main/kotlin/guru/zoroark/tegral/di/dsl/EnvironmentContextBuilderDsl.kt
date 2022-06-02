package guru.zoroark.tegral.di.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.InvalidDeclarationException
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EnvironmentContext

/**
 * Builder DSL for creating an environment.This part of the DSL is specifically responsible for creating an
 * [EnvironmentContext].
 */
@TegralDsl
class EnvironmentContextBuilderDsl : Buildable<EnvironmentContext>, ContextBuilderDsl {
    private val declaredComponents = mutableListOf<Declaration<*>>()

    @TegralDsl
    override fun <T : Any> put(declaration: Declaration<T>) {
        if (declaredComponents.any { it.identifier == declaration.identifier }) {
            throw InvalidDeclarationException(
                "Duplicate identifier: Tried to put '${declaration.identifier}', but one was already present"
            )
        }
        declaredComponents.add(declaration)
    }

    override fun build(): EnvironmentContext {
        val results = declaredComponents.associateBy { it.identifier }
        return EnvironmentContext(results)
    }
}
