package guru.zoroark.tegral.web.appdsl

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ConfigSource
import guru.zoroark.tegral.config.core.RootConfiguration
import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.featureful.Feature
import guru.zoroark.tegral.web.appdefaults.TegralConfigurationContainer
import kotlin.reflect.KClass

/**
 * DSL interface for configurating Tegral applications.
 */
interface TegralApplicationDsl : ExtensibleContextBuilderDsl {
    /**
     * Sets the class to use for loading the configuration classes.
     *
     * This class must be a `data class` that implements [RootConfiguration].
     *
     * By default, [applyDefaults] will set this to be [TegralConfigurationContainer], which only contains a single
     * `tegral: TegralConfig` property.
     */
    fun <T : RootConfiguration> useConfiguration(
        configClass: KClass<T>,
        configuration: ConfigLoaderBuilder.() -> Unit = {}
    )

    /**
     * Adds a feature that will be installed in the application upon build.
     */
    fun install(featureBuilder: Buildable<Feature>)

    /**
     * List of all the Hoplite configuration sources that will be used to load this application.
     *
     * @see useConfiguration
     */
    val configSources: MutableList<ConfigSource>
}

/**
 * Equivalent to [TegralApplicationDsl.useConfiguration], but uses a reified type instead.
 *
 * For example, `useConfiguration<MyConfig>()` is strictly equivalent to calling `useConfiguration(MyConfig::class)`.
 */
inline fun <reified T : RootConfiguration> TegralApplicationDsl.useConfiguration(
    noinline configuration: ConfigLoaderBuilder.() -> Unit = {}
) {
    useConfiguration(T::class, configuration)
}

/**
 * Install the feature onto this application.
 *
 * @see TegralApplicationDsl.install
 */
fun TegralApplicationDsl.install(feature: Feature) {
    install(Buildable.of(feature))
}
