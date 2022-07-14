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

package guru.zoroark.tegral.di

// A utility class, similar to `lazy`, but with an undefined initializer until use is required.
internal class DynLazy<T> {
    companion object {
        object NotInitialized
    }

    private var value: Any? = NotInitialized

    @Suppress("UNCHECKED_CAST")
    inline fun getOrInitialize(initializer: () -> T): T {
        val value1 = value
        if (value1 != NotInitialized) {
            return value as T
        }

        return synchronized(this) {
            val value2 = value
            if (value2 != NotInitialized) {
                value2 as T
            } else {
                val newValue = initializer()
                value = newValue
                newValue
            }
        }
    }
}
