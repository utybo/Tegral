package guru.zoroark.tegral.web.apptest

import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.services.useServices

object TestServicesFeature : IntegrationTestFeature {
    override fun ExtensibleContextBuilderDsl.install() {
        useServices()
    }
}
