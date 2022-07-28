package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityScheme

interface SecuritySchemeDsl {
    var type: SecurityScheme.Type?
    var description: String?
    var name: String?
    var inLocation: SecurityScheme.In?
    var bearerFormat: String?
    var scheme: String?
    var openIdConnectUrl: String?
    var flows: OAuthFlows?
    // TODO proper dsl for oauth flows
}

inline val SecuritySchemeDsl.httpType: Unit
    get() {
        type = SecurityScheme.Type.HTTP
    }

inline val SecuritySchemeDsl.apiKeyType: Unit
    get() {
        type = SecurityScheme.Type.APIKEY
    }

inline val SecuritySchemeDsl.oauth2Type: Unit
    get() {
        type = SecurityScheme.Type.OAUTH2
    }

inline val SecuritySchemeDsl.openIdConnectType: Unit
    get() {
        type = SecurityScheme.Type.OPENIDCONNECT
    }

inline val SecuritySchemeDsl.inCookie: Unit
    get() {
        inLocation = SecurityScheme.In.COOKIE
    }

inline val SecuritySchemeDsl.inHeader: Unit
    get() {
        inLocation = SecurityScheme.In.HEADER
    }

inline val SecuritySchemeDsl.inQuery: Unit
    get() {
        inLocation = SecurityScheme.In.QUERY
    }

class SecuritySchemeBuilder : SecuritySchemeDsl, Builder<SecurityScheme> {
    override var type: SecurityScheme.Type? = null
    override var description: String? = null
    override var name: String? = null
    override var inLocation: SecurityScheme.In? = null
    override var bearerFormat: String? = null
    override var openIdConnectUrl: String? = null
    override var scheme: String? = null
    override var flows: OAuthFlows? = null

    override fun build(): SecurityScheme {
        return SecurityScheme().apply {
            type = this@SecuritySchemeBuilder.type
            description = this@SecuritySchemeBuilder.description
            name = this@SecuritySchemeBuilder.name
            `in` = this@SecuritySchemeBuilder.inLocation
            bearerFormat = this@SecuritySchemeBuilder.bearerFormat
            scheme = this@SecuritySchemeBuilder.scheme
            openIdConnectUrl = this@SecuritySchemeBuilder.openIdConnectUrl
            flows = this@SecuritySchemeBuilder.flows
        }
    }
}
