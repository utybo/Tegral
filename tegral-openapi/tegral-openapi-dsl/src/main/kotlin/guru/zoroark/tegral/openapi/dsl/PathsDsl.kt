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

package guru.zoroark.tegral.openapi.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import io.swagger.v3.oas.models.Paths

/**
 * DSL for the [paths object](https://spec.openapis.org/oas/v3.1.0#paths-object).
 */
@TegralDsl
interface PathsDsl {
    /**
     * Adds a path with the given string and registers any operations defined in the block.
     */
    @TegralDsl
    operator fun String.invoke(path: PathDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "GET" operation on it.
     */
    @TegralDsl
    infix fun String.get(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "POST" operation on it.
     */
    @TegralDsl
    infix fun String.post(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "PUT" operation on it.
     */
    @TegralDsl
    infix fun String.put(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "DELETE" operation on it.
     */
    @TegralDsl
    infix fun String.delete(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "PATCH" operation on it.
     */
    @TegralDsl
    infix fun String.patch(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "OPTIONS" operation on it.
     */
    @TegralDsl
    infix fun String.options(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "HEAD" operation on it.
     */
    @TegralDsl
    infix fun String.head(path: OperationDsl.() -> Unit)
}

/**
 * Builder for [PathsDsl].
 */
class PathsBuilder(private val context: OpenApiDslContext) : PathsDsl, Buildable<Paths> {
    private val pathBuilders = mutableMapOf<String, PathBuilder>()

    override fun String.invoke(path: PathDsl.() -> Unit) {
        pathBuilders.getOrPut(this) { PathBuilder(context) }.path()
    }

    override fun String.get(path: OperationDsl.() -> Unit) {
        this { get(path) }
    }

    override fun String.post(path: OperationDsl.() -> Unit) {
        this { post(path) }
    }

    override fun String.put(path: OperationDsl.() -> Unit) {
        this { put(path) }
    }

    override fun String.delete(path: OperationDsl.() -> Unit) {
        this { delete(path) }
    }

    override fun String.patch(path: OperationDsl.() -> Unit) {
        this { patch(path) }
    }

    override fun String.options(path: OperationDsl.() -> Unit) {
        this { options(path) }
    }

    override fun String.head(path: OperationDsl.() -> Unit) {
        this { head(path) }
    }

    override fun build(): Paths {
        val result = Paths()
        for ((key, builder) in pathBuilders) {
            val path = builder.build()
            result[key] = path
        }
        return result
    }
}
