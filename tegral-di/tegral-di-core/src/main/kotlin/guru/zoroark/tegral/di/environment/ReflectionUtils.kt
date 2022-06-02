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

import kotlin.reflect.KClass

/**
 * Utility function - asserts that [obj] is an instance of [kclass]. Throws an exception if it is not or returns
 * [obj] cast to [T] on success.
 *
 * @param kclass The KClass of the expected type of [obj].
 * @param obj The object that should be tested.
 * @param T The expected type of [obj].
 */
fun <T : Any> ensureInstance(kclass: KClass<T>, obj: Any): T {
    require(kclass.isInstance(obj)) {
        "Object does not correspond to expected type. Expected type ${kclass.qualifiedName} " +
            "but got ${obj.javaClass.name}."
    }
    @Suppress("UNCHECKED_CAST") // The isInstance check is effectively the cast check
    return obj as T
}
