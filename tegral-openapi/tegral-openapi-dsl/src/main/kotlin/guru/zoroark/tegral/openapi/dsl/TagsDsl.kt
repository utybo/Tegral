package guru.zoroark.tegral.openapi.dsl

interface TagsDsl {
    infix fun String.tag(tagBuilder: TagDsl.() -> Unit)
}
