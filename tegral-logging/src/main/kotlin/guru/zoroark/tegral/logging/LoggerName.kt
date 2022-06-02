package guru.zoroark.tegral.logging

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Annotation that can be used to annotate a Tegral DI class and use a custom logger.
 *
 * Note that this only affects loggers retrieved by using:
 *
 * ```kotlin
 * private val logger: Logger by scope.factory()
 * ```
 *
 * It has no effect when using SLF4J's `LoggerFactory.getLogger(...)`.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoggerName(
    /**
     * The name of the logger to use.
     */
    val name: String
)

/**
 * Computes the logger name for the given class, using (in orrder) a [LoggerName] annotation *or* the qualified name of
 * the class *or* `<anon>`.
 */
val KClass<*>.loggerName: String
    get() = findAnnotation<LoggerName>()?.name ?: qualifiedName ?: "<anon>"
