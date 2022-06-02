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
 * Identifies an injectable component via its type and optionally via other elements called [qualifiers][Qualifier].
 *
 * By default, identifiers use the [empty qualifier object][EmptyQualifier] as a way of saying "there is no qualifier
 * here". You generally do not need to use qualifiers if your environment only contains at most one object of a specific
 * type. If you do need multiple objects of the same type, qualifiers such as the [NameQualifier] should be used to
 * differentiate them.
 *
 * @property kclass The class this identifier wraps
 * @property qualifier The qualifier for this identifier.
 */
data class Identifier<T : Any>(val kclass: KClass<T>, val qualifier: Qualifier = EmptyQualifier) {
    override fun toString(): String {
        return (kclass.qualifiedName ?: "<anonymous>") + " ($qualifier)"
    }
}
