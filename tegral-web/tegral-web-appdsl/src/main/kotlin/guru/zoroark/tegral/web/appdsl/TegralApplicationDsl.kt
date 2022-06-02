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
     * Sets the class to use for loading the configuration classes and optionally applies custom logic to the Hoplite
     * `ConfigLoaderBuilder` instance.
     *
     * This class must be a `data class` that implements [RootConfig].
     *
     * By default, [applyDefaults] will set this to be [TegralConfigurationContainer], which only contains a single
     * `tegral: TegralConfig` property.
     */
    fun <T : RootConfig> useConfiguration(
        configClass: KClass<T>,
        configuration: ConfigLoaderBuilder.() -> Unit = {}
    )

    /**
     * Configures the `ConfigLoaderBuilder` used to load application configuration using the given lambda.
     */
    fun useConfiguration(configuration: ConfigLoaderBuilder.() -> Unit)

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
inline fun <reified T : RootConfig> TegralApplicationDsl.useConfiguration(
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
