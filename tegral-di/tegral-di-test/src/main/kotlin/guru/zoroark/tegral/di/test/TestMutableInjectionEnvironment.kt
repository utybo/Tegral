package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionEnvironment

interface TestMutableInjectionEnvironment : InjectionEnvironment, ContextBuilderDsl {
    val components: Map<Identifier<*>, Any>
}
