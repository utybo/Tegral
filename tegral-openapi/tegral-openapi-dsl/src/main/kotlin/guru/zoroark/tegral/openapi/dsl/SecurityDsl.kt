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

import guru.zoroark.tegral.core.TegralDsl
import io.swagger.v3.oas.models.security.SecurityRequirement

@TegralDsl
interface SecurityDsl {
    /**
     * A declaration of which security mechanisms can be used for this operation.
     *
     * - This list behaves like an "OR", only one needs to be fulfilled for the operation.
     * - Requirements defined in the individual `SecurityRequirement` objects behave like an "AND", and all of them need
     * to be fulfilled.
     */
    @TegralDsl
    val securityRequirements: MutableList<SecurityRequirement>

    /**
     * Adds a security requirement object to this operation with the given key.
     */
    @TegralDsl
    fun security(key: String)

    /**
     * Adds a security requirement object to this operation with the given key and scopes.
     */
    @TegralDsl
    fun security(key: String, vararg scopes: String)

    @TegralDsl
    fun security(builder: SecurityRequirementsBuilder.() -> Unit)
}

class SecurityRequirementsBuilder {
    private val securityRequirement = SecurityRequirement()

    fun requirement(key: String) {
        securityRequirement.addList(key)
    }

    fun requirement(key: String, vararg scopes: String) {
        securityRequirement.addList(key, scopes.asList())
    }

    fun build(): SecurityRequirement = securityRequirement
}
