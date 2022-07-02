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

package guru.zoroark.tegral.web.appdefaults

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.featureful.ConfigurableFeature
import guru.zoroark.tegral.logging.LoggingFeature
import guru.zoroark.tegral.services.feature.ServicesFeature
import guru.zoroark.tegral.web.config.WebConfiguration
import guru.zoroark.tegral.web.controllers.WebControllersFeature

/**
 * Feature for Tegral AppDefaults. Adds related services to the environment.
 *
 * See the Tegral AppDefaults documentation for more information.
 */
object AppDefaultsFeature : ConfigurableFeature {
    override val id = "tegral-appdefaults"
    override val name = "Tegral AppDefaults"
    override val description = "Provides sane, overridable defaults and essentials to build a Tegral application."
    override val dependencies = setOf(ServicesFeature, WebControllersFeature, LoggingFeature)
    override val configurationSections = listOf(WebConfiguration)

    override fun ExtensibleContextBuilderDsl.install() {
        put(::DefaultKtorApplication)
        put(::DefaultAppDefaultsModule)
        put(::KeepAliveService)
    }
}
