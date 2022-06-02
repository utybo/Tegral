package guru.zoroark.tegral.di.environment

import kotlin.properties.ReadOnlyProperty

/**
 * An injector is a read-only property delegator that has constraints on [T].
 *
 * Injectors are requested by component classes using any `inject` construct (e.g. [SComponent.inject] or [inject]). In
 * the environment, injectors are created using [InjectionEnvironment.createInjector].
 *
 * @param T The object type to inject
 */
fun interface Injector<out T : Any?> : ReadOnlyProperty<Any?, T>
