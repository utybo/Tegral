package guru.zoroark.tegral.logging

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoggerName(val name: String)

val KClass<*>.loggerName: String
    get() = findAnnotation<LoggerName>()?.name ?: qualifiedName ?: "<anon>"
