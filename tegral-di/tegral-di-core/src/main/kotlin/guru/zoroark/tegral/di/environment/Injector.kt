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

import kotlin.properties.ReadOnlyProperty

/**
 * An injector is a read-only property delegator that has constraints on [T].
 *
 * Injectors are requested by component classes using any `inject` construct (e.g. [SComponent.inject] or [inject]). In
 * the environment, injectors are created using [InjectionEnvironment.createInjector].
 *
 * @param T The object type to inject
 */
fun interface Injector<out T : Any?> : ReadOnlyProperty<Any?, T>
