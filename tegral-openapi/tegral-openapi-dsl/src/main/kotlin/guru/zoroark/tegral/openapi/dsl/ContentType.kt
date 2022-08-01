package guru.zoroark.tegral.openapi.dsl

@JvmInline
value class ContentType(val contentType: String) {
    infix fun or(other: ContentType): MultiContentType = MultiContentType(listOf(this, other))
}

interface PredefinedContentTypesDsl {
    val xml get() = ContentType("application/xml")
    val json get() = ContentType("application/json")
    val form get() = ContentType("application/x-www-form-urlencoded")
}
