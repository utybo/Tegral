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
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.InjectableModule

/**
 * DSL builder class for [InjectableModule]s. It does not add functionality other than the building logic. All DSL
 * functionalities are provided as extension functions of [ContextBuilderDsl].
 */
@TegralDsl
class ModuleBuilderDsl(private val name: String) : ContextBuilderDsl, Buildable<InjectableModule> {
    private val declarations = mutableListOf<Declaration<*>>()
    override fun <T : Any> put(declaration: Declaration<T>) {
        declarations += declaration
    }

    override fun build(): InjectableModule = InjectableModule(name, declarations)
}
