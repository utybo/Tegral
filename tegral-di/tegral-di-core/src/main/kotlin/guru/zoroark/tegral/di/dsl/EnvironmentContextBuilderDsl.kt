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

package guru.zoroark.tegral.di.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.InvalidDeclarationException
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EnvironmentContext

/**
 * Builder DSL for creating an environment.This part of the DSL is specifically responsible for creating an
 * [EnvironmentContext].
 */
@TegralDsl
class EnvironmentContextBuilderDsl : Buildable<EnvironmentContext>, ContextBuilderDsl {
    private val declaredComponents = mutableListOf<Declaration<*>>()

    @TegralDsl
    override fun <T : Any> put(declaration: Declaration<T>) {
        if (declaredComponents.any { it.identifier == declaration.identifier }) {
            throw InvalidDeclarationException(
                "Duplicate identifier: Tried to put '${declaration.identifier}', but one was already present"
            )
        }
        declaredComponents.add(declaration)
    }

    override fun build(): EnvironmentContext {
        val results = declaredComponents.associateBy { it.identifier }
        return EnvironmentContext(results)
    }
}
