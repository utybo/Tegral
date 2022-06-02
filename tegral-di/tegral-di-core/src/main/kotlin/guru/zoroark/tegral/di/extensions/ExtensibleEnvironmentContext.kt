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

import guru.zoroark.tegral.di.environment.Declarations
import guru.zoroark.tegral.di.environment.EnvironmentContext

/**
 * A context for [ExtensibleInjectionEnvironment]s. Provides both the regular set of declarations plus a subcontext for
 * the meta-environment.
 */
class ExtensibleEnvironmentContext(
    /**
     * The declarations for this context, similar to [EnvironmentContext.declarations].
     */
    val declarations: Declarations,
    /**
     * A (non-extensible) environment context for use in the meta-environment.
     */
    val metaContext: EnvironmentContext
)
