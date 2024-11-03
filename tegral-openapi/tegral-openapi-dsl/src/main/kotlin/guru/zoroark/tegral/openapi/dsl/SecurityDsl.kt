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

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import io.swagger.v3.oas.models.security.SecurityRequirement

/**
 * DSL for the [security item object](https://spec.openapis.org/oas/v3.1.0#security-requirement-object).
 *
 * Can be defined at root level (see [RootDsl]) and operation level (see [OperationDsl]).
 */
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

    /**
     * Adds a security requirement object using the provided builder.
     * Allows to define multiple requirements (which behave like an "AND", and all of them need to be fulfilled).
     */
    @TegralDsl
    fun security(builder: SecurityRequirementsBuilder.() -> Unit)
}

/**
 * Builder for [SecurityRequirement]
 */
class SecurityRequirementsBuilder : Buildable<SecurityRequirement> {
    private val securityRequirement = SecurityRequirement()

    /**
     * Adds a security requirement object with the given key.
     */
    fun requirement(key: String) {
        securityRequirement.addList(key)
    }

    /**
     * Adds a security requirement object with the given key and scopes.
     */
    fun requirement(key: String, vararg scopes: String) {
        securityRequirement.addList(key, scopes.asList())
    }

    override fun build(): SecurityRequirement = securityRequirement
}
