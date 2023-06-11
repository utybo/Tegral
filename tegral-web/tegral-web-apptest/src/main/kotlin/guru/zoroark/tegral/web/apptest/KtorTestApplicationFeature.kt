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

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.with
import guru.zoroark.tegral.di.services.noService
import guru.zoroark.tegral.web.appdefaults.DefaultAppDefaultsModule
import guru.zoroark.tegral.web.appdefaults.DefaultKtorApplication
import guru.zoroark.tegral.web.controllers.KtorExtension

/**
 * AppDefaults-equivalent feature for integration testing environment. This sets up:
 *
 * - The [KtorExtension]
 * - A [KtorTestApplication] for the default application
 * - The default application itself (with services disabled)
 * - The [DefaultAppDefaultsModule], which sets up default values for the application.
 */
object KtorTestApplicationFeature : IntegrationTestFeature {
    override fun ExtensibleContextBuilderDsl.install() {
        meta { put { KtorExtension(scope, true) } }
        put(::DefaultKtorTestApplication)
        put(::DefaultKtorApplication) with noService
        put(::DefaultAppDefaultsModule)
    }
}
