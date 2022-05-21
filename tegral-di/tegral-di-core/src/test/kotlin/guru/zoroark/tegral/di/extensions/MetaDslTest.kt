package guru.zoroark.tegral.di.extensions

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.get
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MetaDslTest {
    @Test
    fun `Test meta DSL`() {
        val env = tegralDi {
            meta {
                put { "Hello" }
            }
        }
        assertEquals("Hello", env.metaEnvironment.get())
    }
}
