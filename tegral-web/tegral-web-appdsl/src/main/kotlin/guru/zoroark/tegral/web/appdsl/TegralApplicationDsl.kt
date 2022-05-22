package guru.zoroark.tegral.web.appdsl

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ConfigSource
import guru.zoroark.tegral.config.core.RootConfiguration
import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.featureful.Feature
import kotlin.reflect.KClass

interface TegralApplicationDsl : ExtensibleContextBuilderDsl {
    fun <T : RootConfiguration> useConfiguration(
        configClass: KClass<T>,
        configuration: ConfigLoaderBuilder.() -> Unit = {}
    )

    fun install(featureBuilder: Buildable<Feature>)
    val configSources: MutableList<ConfigSource>
}

inline fun <reified T : RootConfiguration> TegralApplicationDsl.useConfiguration(
    noinline configuration: ConfigLoaderBuilder.() -> Unit = {}
) {
    useConfiguration(T::class, configuration)
}

fun TegralApplicationDsl.install(feature: Feature) {
    install(Buildable.of(feature))
}
