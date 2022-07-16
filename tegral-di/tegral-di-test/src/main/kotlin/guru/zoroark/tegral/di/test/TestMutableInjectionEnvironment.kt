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

package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.resolvers.IdentifierResolver
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment

/**
 * Interface for common utilities in test Tegral DI environments, such as [UnsafeMutableEnvironment]. This is mostly
 * intended to ease writing interfaces that combine further DSLs into a single interface (such as
 * `ControllerTestContext` from Tegral Web Controllers).
 */
interface TestMutableInjectionEnvironment : ExtensibleInjectionEnvironment, ContextBuilderDsl {
    /**
     * The components that have been registered in this environment.
     */
    val components: Map<Identifier<*>, IdentifierResolver<*>>
}
