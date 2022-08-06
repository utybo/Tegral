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

package guru.zoroark.tegral.openapi.feature

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.featureful.Feature

/**
 * A feature for Tegral OpenAPI + Ktor integration in Tegral Web applications.
 *
 * When installing this feature, the following endpoints will be served:
 *
 * - `/_t/openapi`
 * - `/_t/swagger`
 */
object OpenApiFeature : Feature {
    override val id = "tegral-openapi"
    override val name = "Tegral OpenAPI"
    override val description = "Provides OpenAPI and Swagger support for Tegral Web applications"

    override fun ExtensibleContextBuilderDsl.install() {
        put(::OpenApiModule)
        put(::OpenApiEndpoints)
    }
}
