package guru.zoroark.tegral.web.controllers

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.featureful.Feature
import guru.zoroark.tegral.services.feature.ServicesFeature

object WebControllersFeature: Feature {
    override val id = "tegral.web.controllers"
    override val name = "Tegral Web Controllers"
    override val description = """
        Abstractions and DI extension to easily manage Ktor applications, modules and controllers within a Tegral DI environment.
    """.trimIndent()
    override val dependencies = setOf(ServicesFeature)

    override fun ExtensibleContextBuilderDsl.install() {
        meta { put(::KtorExtension)}
    }
}
