package guru.zoroark.tegral.openapi.dsl

@JvmInline
value class MultiContentType(val types: List<ContentType>) {
    infix fun or(other: ContentType): MultiContentType = MultiContentType(types + other)
}
