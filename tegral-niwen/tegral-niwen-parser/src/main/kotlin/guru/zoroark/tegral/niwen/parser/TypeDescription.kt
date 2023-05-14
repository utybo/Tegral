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

package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey

/**
 * The description for the creation of a type
 */
class TypeDescription<T>(
    /**
     * The arguments: that is, everything that was expected and stored using
     * the `storeIn`/`storeValueIn` constructs.
     */
    val arguments: Map<NodeParameterKey<T, *>, *>
) {
    /**
     * Retrieve a value using a key, without type-checking.
     *
     * This is an escape hatch. Consider using the [get] operator instead.
     */
    fun getUntypedValueOrThrow(key: NodeParameterKey<T, *>): Any? {
        return arguments.getOrElse(key) {
            throw NiwenParserException("Key '$key' does not exist in the stored arguments")
        }
    }

    /**
     * Retrieve the given argument, casting it to `T` automatically
     */
    inline operator fun <reified R> get(parameterKey: NodeParameterKey<T, R>): R {
        when (val value = getUntypedValueOrThrow(parameterKey)) {
            is R -> return value // Auto-cast by Kotlin

            else -> throw NiwenParserException(
                "Expected $parameterKey to be of type ${R::class}, but it is actually of type " +
                    value?.let { it::class }
            )
        }
    }
}
