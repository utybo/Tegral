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

package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.TypeDescription
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.typeOf

/**
 * Returns a key for [self] and [subtype].
 */
fun <T, R : T> selfKeyFor(type: KType) = NodeParameterKey<T, R>(type, "self")

/**
 * Used with `storeIn self()`, provides the actual value of a node declaration that is implemented with `by subtype()`
 */
@TegralDsl
inline fun <reified T, R : T> ExpectationReceiver<T>.self(): NodeParameterKey<T, R> {
    return selfKeyFor(typeOf<T>())
}

/**
 * When paired with sealed classes and [self], allows defining abstract nodes that only exist via their subtypes.
 *
 * Consider the following sealed class hierarchy:
 *
 * ```kotlin
 * sealed class Expression(...) {
 *     companion object : ParserNodeDeclaration<Expression> by subtype()
 * }
 *
 * data class Operation(...) : Expression(...) {
 *     companion object : ParserNodeDeclaration<Operation> by reflective()
 * }
 *
 * data class Value(...) : Expression(...) {
 *     companion object : ParserNodeDeclaration<Value> by reflective
 * }
 * ```
 *
 * You can define `Expression` like so:
 *
 * ```kotlin
 * niwenParser {
 *     Expression {
 *         either {
 *             expect(Operation) storeIn self()
 *         } or {
 *             expect(Value) storeIn self()
 *         }
 *     }
 *
 *     Operation {
 *         // ...
 *     }
 *
 *     Value {
 *         // ...
 *     }
 * }
 * ```
 */
@TegralDsl
inline fun <reified T> subtype(): ParserNodeDeclaration<T> {
    val type = typeOf<T>()
    return object : ParserNodeDeclaration<T> {
        override fun make(args: TypeDescription<T>): T {
            return args[selfKeyFor(type)]
        }

        override val nodeName = T::class.simpleName ?: T::class.jvmName
    }
}
