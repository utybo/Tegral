package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License

interface InfoDsl {
    var title: String?
    var description: String?
    var termsOfService: String?
    var version: String?

    var contactName: String?
    var contactUrl: String?
    var contactEmail: String?

    var licenseName: String?
    var licenseUrl: String?
}

@KoaDsl
class InfoBuilder : Builder<Info>, InfoDsl {
    override var title: String? = null
    override var description: String? = null
    override var termsOfService: String? = null
    override var version: String? = null

    override var contactName: String? = null
    override var contactUrl: String? = null
    override var contactEmail: String? = null

    override var licenseName: String? = null
    override var licenseUrl: String? = null

    override fun build(): Info = Info().apply {
        title = this@InfoBuilder.title
        description = this@InfoBuilder.description
        termsOfService = this@InfoBuilder.termsOfService
        version = this@InfoBuilder.version

        if (contactName != null || contactUrl != null || contactEmail != null) {
            contact = Contact().apply {
                name = contactName
                url = contactUrl
                email = contactEmail
            }
        }

        if (licenseName != null || licenseUrl != null) {
            license = License().apply {
                name = licenseName
                url = licenseUrl
            }
        }
    }
}
