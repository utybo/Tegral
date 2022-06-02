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
