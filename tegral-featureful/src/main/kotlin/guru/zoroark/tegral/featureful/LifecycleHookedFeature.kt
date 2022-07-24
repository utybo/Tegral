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

import guru.zoroark.tegral.config.core.RootConfig
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment

/**
 * Features that require special hooking
 */
interface LifecycleHookedFeature : Feature {
    /**
     * Called when the configuration for the application is loaded. This is called before any service is started.
     *
     * Note that this should not be used if your code could instead be placed in the `start` function of a service.
     */
    fun onConfigurationLoaded(configuration: RootConfig) {}

    /**
     * Called just before an application is started (after `onConfigurationLoaded`, before the `startAll` call).
     */
    fun beforeStart(env: ExtensibleInjectionEnvironment) {}
}
