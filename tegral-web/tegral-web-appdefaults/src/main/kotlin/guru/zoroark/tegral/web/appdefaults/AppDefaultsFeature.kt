package guru.zoroark.tegral.web.appdefaults

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.featureful.ConfigurableFeature
import guru.zoroark.tegral.logging.LoggingFeature
import guru.zoroark.tegral.services.feature.ServicesFeature
import guru.zoroark.tegral.web.config.WebConfiguration
import guru.zoroark.tegral.web.controllers.WebControllersFeature

object AppDefaultsFeature : ConfigurableFeature {
    override val id = "tegral-appdefaults"
    override val name = "Tegral AppDefaults"
    override val description = "Provides sane, overridable defaults and essentials to build a Tegral application."
    override val dependencies = setOf(ServicesFeature, WebControllersFeature, LoggingFeature)
    override val configurationSections = listOf(WebConfiguration)

    override fun ExtensibleContextBuilderDsl.install() {
        put(::DefaultKtorApplication)
        put(::KeepAliveService)
    }
}
