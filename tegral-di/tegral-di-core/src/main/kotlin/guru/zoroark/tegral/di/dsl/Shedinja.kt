package guru.zoroark.tegral.di.dsl

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionEnvironmentKind
import guru.zoroark.tegral.di.environment.MixedImmutableEnvironment
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.ExtensibleEnvironmentContextBuilderDsl
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironmentKind

/**
 * Entry point for the Tegral DI DSL, used to build an injection environment.
 *
 * The lambda passed in this function receives an [EnvironmentContextBuilderDsl] object, which can be used to add
 * elements to the built environment.
 *
 * This entry point is compatible with installable extensions.
 *
 * @returns A [MixedImmutableEnvironment] initialized using the given builder.
 */
@TegralDsl
fun tegralDi(builder: ExtensibleContextBuilderDsl.() -> Unit): MixedImmutableEnvironment =
    tegralDi(MixedImmutableEnvironment, builder)

/**
 * Entry point for the Tegral DI DSL, used to build an injection environment.
 *
 * The lambda passed in this function receives an [EnvironmentContextBuilderDsl] object, which can be used to add
 * elements to the built environment.
 *
 * This variation of the `tegralDi` function allows you to specify a custom environment kind (see
 * [InjectionEnvironmentKind] for more information).
 *
 * This entry point is NOT compatible with installable extensions.
 *
 * @param environmentKind The environment builder that should be used.
 */
@TegralDsl
fun <E : InjectionEnvironment> tegralDi(
    environmentKind: InjectionEnvironmentKind<E>,
    builder: ContextBuilderDsl.() -> Unit
): E {
    val res = EnvironmentContextBuilderDsl().apply(builder).build()
    return environmentKind.build(res)
}

/**
 * Entry point for the Tegral DI DSL, used to build an injection environment.
 *
 * The lambda passed in this function receives an [EnvironmentContextBuilderDsl] object, which can be used to add
 * elements to the built environment.
 *
 * This variation of the `tegralDi` function allows you to specify a custom environment kind (see
 * [InjectionEnvironmentKind] for more information).
 *
 * This entry point is compatible with installable extensions.
 *
 * @param environmentKind The environment builder that should be used.
 */
@TegralDsl
fun <E : ExtensibleInjectionEnvironment> tegralDi(
    environmentKind: ExtensibleInjectionEnvironmentKind<E>,
    builder: ExtensibleContextBuilderDsl.() -> Unit
): E {
    val res = ExtensibleEnvironmentContextBuilderDsl().apply(builder).build()
    return environmentKind.build(res)
}

/**
 * Creates a module using the Tegral DI DSL with an optional name. You can then use the `put` function to add this
 * module to environment builders like the block in [tegralDi].
 */
@TegralDsl
fun tegralDiModule(name: String = "<unnamed module>", builder: ContextBuilderDsl.() -> Unit): InjectableModule =
    ModuleBuilderDsl(name).apply(builder).build()
