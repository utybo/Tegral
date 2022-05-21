package guru.zoroark.tegral.di.extensions

import guru.zoroark.tegral.di.environment.InjectionEnvironmentKind

/**
 * Equivalent of [InjectionEnvironmentKind] for environments that support installable extensions.
 *
 * Should be implemented in the companion object of extensible injection environments.
 *
 * See [ExtensibleInjectionEnvironment] for more details.
 */
interface ExtensibleInjectionEnvironmentKind<E : ExtensibleInjectionEnvironment> {
    /**
     * Builds an extensible environment of type [E] using the given context and given meta-environment kind.
     */
    fun build(
        context: ExtensibleEnvironmentContext,
        metaEnvironmentKind: InjectionEnvironmentKind<*> = EagerImmutableMetaEnvironment
    ): E
}
