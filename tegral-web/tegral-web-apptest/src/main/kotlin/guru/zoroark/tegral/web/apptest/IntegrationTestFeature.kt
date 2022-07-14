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

package guru.zoroark.tegral.web.apptest

import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl

/**
 * An installable module for integration testing.
 *
 * Similar in concept to a Tegral Featureful feature.
 */
interface IntegrationTestFeature {
    /**
     * Installs this feature onto the environment, putting element into the Tegral DI environment and meta-environment.
     */
    fun ExtensibleContextBuilderDsl.install()
}
