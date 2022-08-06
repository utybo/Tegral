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

import guru.zoroark.tegral.openapi.dsl.RootDsl
import guru.zoroark.tegral.openapi.ktor.TegralOpenApiKtor
import guru.zoroark.tegral.openapi.ktorui.TegralSwaggerUiKtor
import guru.zoroark.tegral.web.controllers.KtorModule
import io.ktor.server.application.Application
import io.ktor.server.application.install

/**
 * Ktor module for [OpenApiFeature] that takes care of installing things.
 */
class OpenApiModule : KtorModule() {
    override fun Application.install() {
        install(TegralOpenApiKtor) {
            useDefaultDescription()
        }

        install(TegralSwaggerUiKtor)
    }
}

private fun RootDsl.useDefaultDescription() {
    title = "Tegral application"
    description = "*This [Tegral](https://tegral.zoroark.guru) application does not provide a description. Please " +
        "refer to [this]() page for more information*" // TODO add link
    version = "0.0.0"
}
