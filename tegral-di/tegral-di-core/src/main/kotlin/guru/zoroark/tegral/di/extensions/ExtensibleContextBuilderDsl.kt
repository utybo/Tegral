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

package guru.zoroark.tegral.di.extensions

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.dsl.EnvironmentContextBuilderDsl
import guru.zoroark.tegral.di.environment.Declaration

/**
 * A context builder which can also receive meta-environment components via the [meta] function.
 */
@TegralDsl
interface ExtensibleContextBuilderDsl : ContextBuilderDsl {
    /**
     * Executes the given lambda (which takes a [ContextBuilderDsl]) to execute actions on the
     * meta-environment.
     *
     * For example, you can add a component to the meta-environment like so:
     *
     * ```
     * meta {
     *     put(::MyComponent)
     * }
     * ```
     */
    @TegralDsl
    fun meta(action: ContextBuilderDsl.() -> Unit)
}

/**
 * Default builder for extensible environment contexts using the DSL.
 */
@TegralDsl
class ExtensibleEnvironmentContextBuilderDsl : ExtensibleContextBuilderDsl, Buildable<ExtensibleEnvironmentContext> {
    private val regularContextBuilder = EnvironmentContextBuilderDsl()
    private val metaContextBuilder = EnvironmentContextBuilderDsl()

    override fun meta(action: ContextBuilderDsl.() -> Unit) {
        action(metaContextBuilder)
    }

    override fun <T : Any> put(declaration: Declaration<T>) {
        regularContextBuilder.put(declaration)
    }

    override fun build(): ExtensibleEnvironmentContext {
        val regular = regularContextBuilder.build()
        val meta = metaContextBuilder.build()
        return ExtensibleEnvironmentContext(regular.declarations, meta)
    }
}
