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

import guru.zoroark.tegral.config.core.RootConfig
import guru.zoroark.tegral.featureful.Feature

/**
 * The context within which a feature gets installed.
 *
 * The various properties of this object can be used by features to further initialize themselves.
 */
class FeatureContext(
    /**
     * The [RootConfig] loaded by the application.
     */
    val configuration: RootConfig
)

/**
 * Class that stores necessary data for building a [Feature].
 *
 * This is the class used for storing information about how features are configured before building the app environment.
 * For example, this...
 *
 * ```kotlin
 * tegral {
 *     install(MyFeature)
 *     install(MyOtherFeature) { /* ... */ }
 * }
 * ```
 *
 * ... would lead to two FeatureBuilder instances: one with MyFeature and a no-op configBuilder, one with MyOtherFeature
 * and the provided lambda.
 *
 * @type T see [Feature]'s `T` documentation
 */
class FeatureBuilder<T>(
    /**
     * The feature to build
     */
    val feature: Feature<T>,
    /**
     * A lambda to call onto the feature. Used to configure the [T] object.
     */
    val configBuilder: T.(FeatureContext) -> Unit
)
