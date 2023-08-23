/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package guru.zoroark.tegral.web.appdsl

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ConfigSource
import guru.zoroark.tegral.config.core.RootConfig
import guru.zoroark.tegral.config.core.SectionedConfigurationDecoder
import guru.zoroark.tegral.config.core.TegralConfig
import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EmptyQualifier
import guru.zoroark.tegral.di.environment.ScopedSupplier
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.putAlias
import guru.zoroark.tegral.featureful.ConfigurableFeature
import guru.zoroark.tegral.featureful.Feature
import guru.zoroark.tegral.featureful.LifecycleHookedFeature
import guru.zoroark.tegral.featureful.SimpleFeature
import guru.zoroark.tegral.web.appdefaults.AppDefaultsFeature
import guru.zoroark.tegral.web.appdefaults.TegralConfigurationContainer
import kotlin.reflect.KClass

typealias ContextInstallationHook = ExtensibleContextBuilderDsl.() -> Unit

/**
 * This class allows for the dynamic creation of ad-hoc features.
 *
 * Usage of this class is discouraged (follow the guidelines set forth by the [Feature] documentation instead). It is
 * only really useful if you are dynamically creating features, such as when you are using the [tegral] block: your
 * entire application is represented as a feature within the Tegral application.
 */
class TegralCustomFeature(
    override val id: String,
    override val name: String,
    override val description: String,
    private val installationCallback: ContextInstallationHook
) : SimpleFeature {
    override fun ExtensibleContextBuilderDsl.install() {
        installationCallback()
    }
}

/**
 * Builder for a [TegralCustomFeature] specifically tailored to contain the logic from the [tegral] block.
 */
class TegralApplicationFeatureBuilder : ExtensibleContextBuilderDsl, Buildable<FeatureBuilder<Unit>> {
    private val contextBuilderHooks = mutableListOf<ContextInstallationHook>()
    override fun meta(action: ContextBuilderDsl.() -> Unit) {
        contextBuilderHooks += { meta(action) }
    }

    override fun <T : Any> put(declaration: Declaration<T>) {
        contextBuilderHooks += { put(declaration) }
    }

    override fun build(): FeatureBuilder<Unit> {
        return FeatureBuilder(
            TegralCustomFeature(
                id = "app-feature",
                name = "Application Feature",
                description = "This feature contains classes you place in the 'tegral { }' block.",
                installationCallback = {
                    this@TegralApplicationFeatureBuilder.contextBuilderHooks.forEach { it() }
                }
            )
        ) {}
    }
}

/**
 * Builder for a Tegral application. This class allows you to install features, tweak the application's configuration,
 * add modules, classes and components of your own, etc. This is the class that is used as a receiver within the
 * [tegral] block.
 */
class TegralApplicationBuilder : TegralApplicationDsl, Buildable<TegralApplication> {
    private val defaultFeatureBuilder: TegralApplicationFeatureBuilder = TegralApplicationFeatureBuilder()
    private val featuresBuilders: MutableList<FeatureBuilder<*>> = mutableListOf()

    private val config: ConfigLoaderBuilder = ConfigLoaderBuilder.default()
    private var configClass: KClass<out RootConfig> = TegralConfigurationContainer::class
    override val configSources = mutableListOf<ConfigSource>()

    override fun meta(action: ContextBuilderDsl.() -> Unit) {
        defaultFeatureBuilder.meta(action)
    }

    override fun <T : Any> put(declaration: Declaration<T>) {
        defaultFeatureBuilder.put(declaration)
    }

    override fun <T : RootConfig> useConfigurationClass(
        configClass: KClass<T>,
        configuration: ConfigLoaderBuilder.() -> Unit
    ) {
        this.configClass = configClass
        config.configuration()
    }

    override fun useConfiguration(configuration: ConfigLoaderBuilder.() -> Unit) {
        config.configuration()
    }

    override fun <T> install(featureBuilder: FeatureBuilder<T>) {
        featuresBuilders += featureBuilder
    }

    override fun build(): TegralApplication {
        // Build all required features
        val toInstall = featuresBuilders.toMutableSet()
        toInstall += defaultFeatureBuilder.build()

        tailrec fun ensureAllDependenciesPresent(parents: Collection<Feature<*>>) {
            val absentDependencies = parents
                .flatMap { it.dependencies }
                .minus(toInstall.map { it.feature }.toSet())
                .toSet()
            if (absentDependencies.isNotEmpty()) {
                absentDependencies.forEach { toInstall += FeatureBuilder(it as Feature<Any>) {} }
                ensureAllDependenciesPresent(absentDependencies)
            }
        }

        ensureAllDependenciesPresent(toInstall.map { it.feature })

        val toInstallFeatures = toInstall.map { it.feature }

        // Retrieve [tegral.*] configuration sections
        val sections = toInstallFeatures
            .filterIsInstance<ConfigurableFeature<*>>()
            .flatMap { it.configurationSections }
            .distinct()
        // Create a decoder adapted to said sections and add it to the config loader
        config.addDecoder(SectionedConfigurationDecoder(TegralConfig::class, ::TegralConfig, sections.toList()))
        // Allow empty sources to support cases where no configuration file is present
        config.allowEmptySources()

        // Build and load configuration
        val appConfig = config.build().loadConfigOrThrow(configClass, configSources)

        // Trigger lifecycle-hooked features' onConfigurationLoaded functions
        toInstallFeatures
            .filterIsInstance<LifecycleHookedFeature<*>>()
            .forEach { it.onConfigurationLoaded(appConfig) }

        val featureContext = FeatureContext(appConfig)

        val environment = tegralDi {
            // Register the root config class under both its own class type and under RootConfig.
            @Suppress("UNCHECKED_CAST")
            put(this@TegralApplicationBuilder.configClass as KClass<RootConfig>) { appConfig }
            putAlias(aliasClass = RootConfig::class, targetClass = this@TegralApplicationBuilder.configClass)

            put { appConfig.tegral }

            for (featureBuilder in toInstall) {
                meta { unsafePut(featureBuilder.feature::class) { featureBuilder.feature } }
                initAndInstallFeature(featureBuilder, featureContext)
            }
        }
        return TegralApplication(environment)
    }
}

private fun <T> ExtensibleContextBuilderDsl.initAndInstallFeature(
    featureBuilder: FeatureBuilder<T>,
    context: FeatureContext
) {
    val configObj = featureBuilder.feature.createConfigObject()
    featureBuilder.configBuilder(configObj, context)
    with(featureBuilder.feature) { install(configObj) }
}

private fun ContextBuilderDsl.unsafePut(kclass: KClass<*>, provider: ScopedSupplier<*>) {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> genericsHackPut() {
        put(kclass as KClass<T>, EmptyQualifier, provider as ScopedSupplier<T>)
    }
    genericsHackPut<Any>()
}

/**
 * Applies sane defaults to the given application builder. Specifically, this function:
 *
 * - Adds the Tegral Web AppDefaults feature ([AppDefaultsFeature])
 * - Sets [TegralConfigurationContainer] as the configuration class (i.e. your configuration files will be parsed into
 *   a [TegralConfigurationContainer] instance)
 * - Adds the classpath resources `/tegral.toml`, `/tegral.yaml` and `/tegral.json` as configuration sources (if
 *   present).
 *
 * This function is called automatically by [tegral], you should not need to call it again.
 */
@TegralDsl
fun TegralApplicationDsl.applyDefaults() {
    install(FeatureBuilder(AppDefaultsFeature) {})

    useConfigurationType<TegralConfigurationContainer>()

    configSources += listOf("/tegral.toml", "/tegral.yaml", "/tegral.json")
        .flatMap {
            ConfigSource.fromClasspathResources(listOf(it)).fold(
                { listOf() },
                { source -> source }
            )
        }
}
