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

package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KType

/**
 * This interface is passed to most of the builder classes and provides utilities that need to be present through the
 * "DSL tree".
 */
interface KoaDslContext {
    /**
     * Computes (or gets) the schema associated with the given KType. You can get a KType using the `typeOf<>()`
     * function.
     *
     * The schema will be returned as a '$ref', while the actual components are stored inside the context.
     */
    fun computeAndRegisterSchema(type: KType): Schema<*>

    fun persistTo(openApi: OpenAPI)
}
