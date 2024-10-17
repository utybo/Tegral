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
    var securityRequirements: MutableList<SecurityRequirement>

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
}
