package guru.zoroark.tegral.di.environment

/**
 * An environment kind is a facility for building [InjectionEnvironment] in a nicer way. This should be implemented by
 * the companion object of an [InjectionEnvironment] class.
 *
 * @type E The type of the injection environment built by this object.
 */
interface InjectionEnvironmentKind<E : InjectionEnvironment> {
    /**
     * Builds the injection environment using the given context.
     *
     * The implementation should be trivial: just call your injection environment's constructor with the given
     * [context].
     *
     * @param context The context to use for the injection environment.
     */
    fun build(context: EnvironmentContext): E
}
