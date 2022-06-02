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

package guru.zoroark.tegral.web.controllers

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.featureful.Feature
import guru.zoroark.tegral.services.feature.ServicesFeature

/**
 * Feature object for Tegral Web Controllers.
 */
object WebControllersFeature : Feature {
    override val id = "tegral.web.controllers"
    override val name = "Tegral Web Controllers"
    override val description =
        "Abstractions and DI extension to easily manage Ktor applications, modules and controllers within a Tegral " +
            "DI environment."
    override val dependencies = setOf(ServicesFeature)

    override fun ExtensibleContextBuilderDsl.install() {
        meta { put(::KtorExtension) }
    }
}
