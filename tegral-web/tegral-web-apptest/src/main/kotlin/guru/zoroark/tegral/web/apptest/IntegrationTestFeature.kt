package guru.zoroark.tegral.web.apptest

import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl

interface IntegrationTestFeature {
    fun ExtensibleContextBuilderDsl.install()
}
