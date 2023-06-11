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

package guru.zoroark.tegral.featureful

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl

/**
 * A Feature is a Tegral software component that gets installed within an application.
 *
 * Features themselves do things by:
 *
 * - Modifying the application's Tegral DI environment (e.g. adding components or DI extensions)
 *
 * ### Creating a feature
 *
 * Creating a feature can be done by implementing this interface in an `object`, e.g.
 *
 * ```kotlin
 * object MyFeature : Feature {
 *     override val id: String = "my-feature"
 *     override val name: String = "My Feature"
 *     override val description: String = "My feature does this and that"
 *
 *     override fun install(context: FeatureContext) {
 *         // Do something
 *     }
 * }
 * ```
 *
 * Generally, features should not actually perform actions other than creating relevant classes within the DI
 * environment of the application (i.e. the receiver of [install]).
 *
 * ### Configuration
 *
 * There are three ways features can be configured.
 *
 * #### No configuration
 *
 * If your feature cannot be configured in any particular way, you can implement [SimpleFeature] instead of [Feature].
 *
 * #### Configuration via the Tegral configuration file
 *
 * Tegral Web applications are configured using a `tegral.toml` file. If you wish for your feature to be configured this
 * way, extend the [ConfigurableFeature] interface, which itself extends the [Feature] interface.
 *
 * This is recommended for applications whose configuration is expected to change depending on the environment the
 * application may be ran in. See the [12 factors app documentation](https://12factor.net/config) for more information
 * on these concepts.
 *
 * #### Hard-coded configuration
 *
 * For configuration that is expected to never change between environments, features can be configured using a lambda,
 * like so:
 *
 * ```kotlin
 * install(MyFeature) {
 *     myConfigOption = ...
 * }
 * ```
 *
 * When installed using `install(MyFeature)`, users can optionally pass a lambda that will have [T] has a receiver. This
 * lambda can be used to configure pretty much anything.
 *
 * The receiver is created using [createConfigObject], and can be retrieved as one of the parameters of [install].
 */
interface Feature<T> {
    /**
     * The identifier for this feature. Should be a lower case kebab-case string (e.g. `this-is-my-feature`).
     *
     * Please avoid using the `tegral-` prefix, which is reserved for first-party Tegral features.
     */
    val id: String

    /**
     * The name for your feature, e.g. `My Feature`.
     */
    val name: String

    /**
     * A short description of what your feature provides.
     */
    val description: String

    /**
     * Dependencies for this feature. Dependencies are features that will be installed together with (but not
     * necessarily *before*) your feature.
     */
    val dependencies: Set<Feature<*>> get() = setOf()

    /**
     * Create an object that will be used as the receiver parameter for `install(MyFeature) { HERE }`
     */
    fun createConfigObject(): T

    /**
     * Callback when this feature is installed onto an environment.
     */
    @TegralDsl
    fun ExtensibleContextBuilderDsl.install(configuration: T)
}
