package guru.zoroark.tegral.web.apptest

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.with
import guru.zoroark.tegral.di.services.noService
import guru.zoroark.tegral.web.appdefaults.DefaultKtorApplication
import guru.zoroark.tegral.web.controllers.KtorExtension

object KtorTestApplicationFeature : IntegrationTestFeature {
    override fun ExtensibleContextBuilderDsl.install() {
        meta { put(::KtorExtension) }
        put(::DefaultKtorTestApplication)
        put(::DefaultKtorApplication) with noService
    }
}
