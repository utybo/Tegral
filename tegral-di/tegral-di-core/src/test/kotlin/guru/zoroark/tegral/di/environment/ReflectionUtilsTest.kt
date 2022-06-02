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

import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class ReflectionUtilsTest {
    class Unrelated

    open class Grandparent
    open class Parent : Grandparent()
    class Child : Parent()
    object ChildButObject : Parent()

    @Test
    fun `ensureInstance, same class`() {
        assertDoesNotThrow { ensureInstance(Parent::class, Parent()) }
    }

    @Test
    fun `ensureInstance, subclass`() {
        assertDoesNotThrow { ensureInstance(Parent::class, Child()) }
    }

    @Test
    fun `ensureInstance, subclass (object)`() {
        assertDoesNotThrow { ensureInstance(Parent::class, ChildButObject) }
    }

    @Test
    fun `ensureInstance, expect child but get parent`() {
        assertThrows<IllegalArgumentException> { ensureInstance(Parent::class, Grandparent()) }
    }

    @Test
    fun `ensureIsntance, expect child but get unrelated`() {
        assertThrows<IllegalArgumentException> { ensureInstance(Parent::class, Unrelated()) }
    }
}
