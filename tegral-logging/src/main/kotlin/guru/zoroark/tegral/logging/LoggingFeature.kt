package guru.zoroark.tegral.logging

import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.factory.putFactory
import guru.zoroark.tegral.featureful.Feature
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggingFeature : Feature {
    override val id = "tegral-logging"
    override val name = "Tegral Logging"
    override val description = "Provides logging utilities for Tegral applications"

    override fun ExtensibleContextBuilderDsl.install() {
        putFactory<Logger> { requester -> LoggerFactory.getLogger(requester::class.loggerName) }
    }
}
