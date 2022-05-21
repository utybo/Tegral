package guru.zoroark.tegral.di.extensions

import guru.zoroark.tegral.di.environment.Declarations
import guru.zoroark.tegral.di.environment.EnvironmentContext

/**
 * A context for [ExtensibleInjectionEnvironment]s. Provides both the regular set of declarations plus a subcontext for
 * the meta-environment.
 */
class ExtensibleEnvironmentContext(
    /**
     * The declarations for this context, similar to [EnvironmentContext.declarations].
     */
    val declarations: Declarations,
    /**
     * A (non-extensible) environment context for use in the meta-environment.
     */
    val metaContext: EnvironmentContext
)
