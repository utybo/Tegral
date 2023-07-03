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

package guru.zoroark.tegral.di.extensions.fundef

/**
 * Marker annotation for the Fundef feature.
 *
 * You can follow the development of fundefs here: https://github.com/utybo/Tegral/issues/73
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.PROPERTY
)
@RequiresOptIn
annotation class ExperimentalFundef

/**
 * When used on a function, allows to use `put` on the function instead of [putFundef].
 *
 * For example, allows you to go from this:
 *
 * ```kotlin
 * fun myFundef() {
 *     // ...
 * }
 *
 * val env = tegralDi {
 *     putFundef(::myFundef)
 * }
 * ```
 *
 * ... to this...
 *
 * ```kotlin
 * @Fundef
 * fun myFundef() {
 *     // ...
 * }
 *
 * val env = tegralDi {
 *     put(::myFundef)
 * }
 * ```
 */
@Target(AnnotationTarget.FUNCTION)
@ExperimentalFundef
annotation class Fundef
