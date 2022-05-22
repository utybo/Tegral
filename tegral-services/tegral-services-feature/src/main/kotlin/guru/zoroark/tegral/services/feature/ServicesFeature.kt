package guru.zoroark.tegral.services.feature

import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.services.useServices
import guru.zoroark.tegral.featureful.Feature

object ServicesFeature : Feature {
    override val id = "tegral.services"
    override val name = "Tegral Services"
    override val description = "Service DI helpers for Tegral Services and Tegral DI"
    override fun ExtensibleContextBuilderDsl.install() {
        useServices()
    }
}
