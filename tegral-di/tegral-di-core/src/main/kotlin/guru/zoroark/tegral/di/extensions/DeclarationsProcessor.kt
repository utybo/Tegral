package guru.zoroark.tegral.di.extensions

import guru.zoroark.tegral.di.environment.Declaration

/**
 * Interface for classes that need to process some declarations in order to work properly.
 *
 * This class should be implemented by any class present within a meta-environment.
 */
interface DeclarationsProcessor {
    /**
     * Process the given sequence of declarations.
     *
     * This function is called after the initialization of meta-environment components but before the initialization of
     * actual components.
     */
    fun processDeclarations(sequence: Sequence<Declaration<*>>)
}
