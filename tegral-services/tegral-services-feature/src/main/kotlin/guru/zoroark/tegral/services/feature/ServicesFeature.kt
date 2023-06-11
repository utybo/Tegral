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

package guru.zoroark.tegral.services.feature

import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.services.useServices
import guru.zoroark.tegral.featureful.SimpleFeature

/**
 * Feature for using Tegral services in Tegral applications.
 *
 * This feature only installs the Tegral DI Services extension onto this application.
 */
object ServicesFeature : SimpleFeature {
    override val id = "tegral.services"
    override val name = "Tegral Services"
    override val description = "Service DI helpers for Tegral Services and Tegral DI"
    override fun ExtensibleContextBuilderDsl.install() {
        useServices()
    }
}
