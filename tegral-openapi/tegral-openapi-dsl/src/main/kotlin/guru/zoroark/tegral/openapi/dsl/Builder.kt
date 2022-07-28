package guru.zoroark.tegral.openapi.dsl

fun interface Builder<out T> {
    fun build(): T
}
