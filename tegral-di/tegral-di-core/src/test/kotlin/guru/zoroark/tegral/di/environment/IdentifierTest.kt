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

import kotlin.test.Test
import kotlin.test.assertEquals

class IdentifierTest {
    @Test
    fun `ctor, empty qualifier by default`() {
        val identifier = Identifier(String::class)
        assertEquals(EmptyQualifier, identifier.qualifier)
    }
    @Test
    fun `toString, non-anonymous object`() {
        val identifier = Identifier(IdentifierTest::class)
        assertEquals("guru.zoroark.tegral.di.environment.IdentifierTest (<no qualifier>)", identifier.toString())
    }

    @Test
    fun `toString, anonymous object`() {
        val anonymousObject = object {}
        val identifier = Identifier(anonymousObject::class)
        assertEquals("<anonymous> (<no qualifier>)", identifier.toString())
    }
}
