package guru.zoroark.tegral.featureful

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl

interface Feature {
    val id: String
    val name: String
    val description: String

    val dependencies: Set<Feature> get() = setOf()

    @TegralDsl
    fun ExtensibleContextBuilderDsl.install()
}
