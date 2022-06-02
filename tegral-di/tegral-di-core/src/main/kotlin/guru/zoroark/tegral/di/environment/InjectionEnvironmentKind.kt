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
 * An environment kind is a facility for building [InjectionEnvironment] in a nicer way. This should be implemented by
 * the companion object of an [InjectionEnvironment] class.
 *
 * @type E The type of the injection environment built by this object.
 */
interface InjectionEnvironmentKind<E : InjectionEnvironment> {
    /**
     * Builds the injection environment using the given context.
     *
     * The implementation should be trivial: just call your injection environment's constructor with the given
     * [context].
     *
     * @param context The context to use for the injection environment.
     */
    fun build(context: EnvironmentContext): E
}
