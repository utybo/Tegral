package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionEnvironment

/**
 * Interface for common utilities in test Tegral DI environments, such as [UnsafeMutableEnvironment]. This is mostly
 * intended to ease writing interfaces that combine further DSLs into a single interface (such as
 * `ControllerTestContext` from Tegral Web Controllers).
 */
interface TestMutableInjectionEnvironment : InjectionEnvironment, ContextBuilderDsl {
    /**
     * The components that have been registered in this environment.
     */
    val components: Map<Identifier<*>, Any>
}
