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
