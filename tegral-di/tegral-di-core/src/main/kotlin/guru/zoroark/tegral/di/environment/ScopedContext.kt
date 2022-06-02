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

package guru.zoroark.tegral.di.environment

import guru.zoroark.tegral.core.TegralDsl

/**
 * An object that contains a scope. For use as a receiver in lambdas (e.g. [ScopedSupplier])
 */
@TegralDsl
interface ScopedContext {
    /**
     * A scope, represented as an [InjectionScope]. This scope can be used to perform injection.
     */
    @TegralDsl
    val scope: InjectionScope
}

private class SimpleScopedContext(override val scope: InjectionScope) : ScopedContext

/**
 * Creates a `ScopedContext` that contains the given scope as-is as a property.
 */
@Suppress("FunctionNaming")
fun ScopedContext(scope: InjectionScope): ScopedContext = SimpleScopedContext(scope)

/**
 * A supplier of T that takes a [ScopedContext] as a receiver.
 */
typealias ScopedSupplier<T> = ScopedContext.() -> T
