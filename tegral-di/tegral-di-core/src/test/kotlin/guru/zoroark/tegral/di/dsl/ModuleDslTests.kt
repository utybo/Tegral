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

package guru.zoroark.tegral.di.dsl

import guru.zoroark.tegral.di.ExampleClass
import guru.zoroark.tegral.di.ExampleClass2
import guru.zoroark.tegral.di.environment.Identifier
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ModuleDslTests {
    @Test
    fun `Test creating unnamed module`() {
        val module = tegralDiModule {
            put { ExampleClass() }
            put { ExampleClass2() }
        }
        assertEquals("<unnamed module>", module.name)
        assertEquals(2, module.declarations.size)
        assertEquals(Identifier(ExampleClass::class), module.declarations[0].identifier)
        assertEquals(Identifier(ExampleClass2::class), module.declarations[1].identifier)
    }

    @Test
    fun `Test creating named module`() {
        val module = tegralDiModule("Hello") {
            put { ExampleClass() }
            put { ExampleClass2() }
        }
        assertEquals("Hello", module.name)
        assertEquals(2, module.declarations.size)
        assertEquals(Identifier(ExampleClass::class), module.declarations[0].identifier)
        assertEquals(Identifier(ExampleClass2::class), module.declarations[1].identifier)
    }
}
