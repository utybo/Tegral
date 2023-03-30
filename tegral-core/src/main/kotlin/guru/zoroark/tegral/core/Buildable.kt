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

package guru.zoroark.tegral.core

/**
 * Represents a builder that can be turned into an object of type `T`.
 */
fun interface Buildable<out T> {
    /**
     * Build the current object into a [T] object.
     */
    fun build(): T

    companion object {
        /**
         * Creates a [Buildable] object that will always return the given [result].
         */
        fun <T> of(result: T): Buildable<T> = Buildable { result }
    }
}
