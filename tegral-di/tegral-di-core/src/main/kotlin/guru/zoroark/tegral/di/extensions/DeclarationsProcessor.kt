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

import guru.zoroark.tegral.di.environment.Declaration

/**
 * Interface for classes that need to process some declarations in order to work properly.
 *
 * This class should be implemented by any class present within a meta-environment.
 */
interface DeclarationsProcessor {
    /**
     * Process the given sequence of declarations.
     *
     * This function is called after the initialization of meta-environment components but before the initialization of
     * actual components.
     */
    fun processDeclarations(sequence: Sequence<Declaration<*>>)
}
