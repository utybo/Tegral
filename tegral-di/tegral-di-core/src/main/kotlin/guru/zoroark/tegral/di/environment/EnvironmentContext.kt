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
 * Data objects that contain all of the information passed to [InjectionEnvironment] constructors to let them initialize
 * their data.
 *
 * This is the main link between the DSL (which outputs objects of this class) and the actual environments (which
 * consume objects of this class).
 *
 * @property declarations The declarations contained in this context
 */
class EnvironmentContext(val declarations: Declarations)
