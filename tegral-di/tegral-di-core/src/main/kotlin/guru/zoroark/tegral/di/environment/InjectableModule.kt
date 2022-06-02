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

/**
 * A module that can be injected in environments. At their core, injectable modules are just lists of
 * [declarations][Declaration] that get copied over when adding this module to environments.
 *
 * @property name The name of this module. It is only used for debugging purposes. May be empty.
 */
class InjectableModule(val name: String, defs: Collection<Declaration<*>>) {
    /**
     * The declarations contained in this module.
     */
    val declarations: List<Declaration<*>> = defs.toList() // Copy to a list
}
