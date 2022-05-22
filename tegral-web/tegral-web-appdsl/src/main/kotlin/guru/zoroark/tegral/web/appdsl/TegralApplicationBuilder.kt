package guru.zoroark.tegral.web.appdsl

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.ConfigSource
import guru.zoroark.tegral.config.core.RootConfiguration
import guru.zoroark.tegral.config.core.SectionedConfigurationDecoder
import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EmptyQualifier
import guru.zoroark.tegral.di.environment.ScopedSupplier
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.featureful.ConfigurableFeature
import guru.zoroark.tegral.featureful.Feature
import guru.zoroark.tegral.web.appdefaults.AppDefaultsFeature
import guru.zoroark.tegral.web.appdefaults.TegralConfigurationContainer
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

typealias ContextInstallationHook = ExtensibleContextBuilderDsl.() -> Unit

class TegralCustomFeature(
    override val id: String,
    override val name: String,
    override val description: String,
    private val installationCallback: ContextInstallationHook
) : Feature {
    override fun ExtensibleContextBuilderDsl.install() {
        installationCallback()
    }
}

class TegralApplicationFeatureBuilder : ExtensibleContextBuilderDsl, Buildable<Feature> {
    private val contextBuilderHooks = mutableListOf<ContextInstallationHook>()
    override fun meta(action: ContextBuilderDsl.() -> Unit) {
        contextBuilderHooks += { meta(action) }
    }

    override fun <T : Any> put(declaration: Declaration<T>) {
        contextBuilderHooks += { put(declaration) }
    }

    override fun build(): Feature {
        return TegralCustomFeature(
            id = "app-feature",
            name = "Application Feature",
            description = "This feature contains classes you place in the 'tegral { }' block.",
            installationCallback = {
                this@TegralApplicationFeatureBuilder.contextBuilderHooks.forEach { it() }
            }
        )
    }
}

class TegralApplicationBuilder : TegralApplicationDsl, Buildable<TegralApplication> {
    private val defaultFeatureBuilder: TegralApplicationFeatureBuilder = TegralApplicationFeatureBuilder()
    private val featuresBuilders: MutableList<Buildable<Feature>> = mutableListOf()

    private val config: ConfigLoaderBuilder = ConfigLoaderBuilder.default()
    private var configClass: KClass<out RootConfiguration> = TegralConfigurationContainer::class
    override val configSources = mutableListOf<ConfigSource>()

    override fun meta(action: ContextBuilderDsl.() -> Unit) {
        defaultFeatureBuilder.meta(action)
    }

    override fun <T : Any> put(declaration: Declaration<T>) {
        defaultFeatureBuilder.put(declaration)
    }

    override fun <T : RootConfiguration> useConfiguration(
        configClass: KClass<T>,
        configuration: ConfigLoaderBuilder.() -> Unit
    ) {
        this.configClass = configClass
        config.configuration()
    }

    override fun install(featureBuilder: Buildable<Feature>) {
        featuresBuilders += featureBuilder
    }

    override fun build(): TegralApplication {
        // Build all required features
        val toInstall =
            (this@TegralApplicationBuilder.featuresBuilders.toMutableSet() + this@TegralApplicationBuilder.defaultFeatureBuilder)
                .map { it.build() }
                .toMutableSet()

        tailrec fun ensureAllDependenciesPresent(parents: Collection<Feature>) {
            val absentDependencies = parents.flatMap { it.dependencies }.minus(toInstall)
            if (absentDependencies.isNotEmpty()) {
                absentDependencies.forEach { toInstall += it }
                ensureAllDependenciesPresent(absentDependencies)
            }
        }

        ensureAllDependenciesPresent(toInstall)

        // Retrieve [tegral.*] configuration sections
        val sections = toInstall.filterIsInstance<ConfigurableFeature>().flatMap { it.configurationSections }.distinct()
        // Create a decoder adapted to said sections and add it to the config loader
        config.addDecoder(SectionedConfigurationDecoder(sections.toList()))

        // Build and load configuration
        val appConfig = config.build().loadConfigOrThrow(configClass, configSources)

        val environment = tegralDi {
            put { appConfig }
            put { appConfig.tegral }

            toInstall.forEach {
                meta { unsafePut(it::class) { it } }
                with(it) { install() }
            }
        }
        return TegralApplication(environment)
    }
}

private fun ContextBuilderDsl.unsafePut(kclass: KClass<*>, provider: ScopedSupplier<*>) {
    fun <T : Any> genericsHackPut() {
        put(kclass as KClass<T>, EmptyQualifier, provider as ScopedSupplier<T>)
    }
    genericsHackPut<Any>()
}

private val logger = LoggerFactory.getLogger("tegral.web.appdsl.applyDefaults")

@TegralDsl
fun TegralApplicationDsl.applyDefaults() {
    install(AppDefaultsFeature)

    useConfiguration<TegralConfigurationContainer>()

    configSources += listOf("/tegral.toml", "/tegral.yaml", "/tegral.json")
        .flatMap {
            ConfigSource.fromClasspathResources(listOf(it)).fold(
                { listOf() },
                { source -> source }
            )
        }
}
