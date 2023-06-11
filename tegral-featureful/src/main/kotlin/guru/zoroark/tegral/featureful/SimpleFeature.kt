package guru.zoroark.tegral.featureful

import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl

/**
 * A simplified interface for [Feature] that cannot be configured.
 *
 * In reality, the type of the configuration is [Unit]. If you need to use [SimpleFeature] in combination with
 * [ConfigurableFeature] or other specializations of [Feature], use `Unit` as the type parameter.
 */
interface SimpleFeature : Feature<Unit> {
    override fun createConfigObject() = Unit

    /**
     * Identical to [Feature.install], but without a configuration object.
     */
    fun ExtensibleContextBuilderDsl.install()

    override fun ExtensibleContextBuilderDsl.install(configuration: Unit) {
        install()
    }
}
