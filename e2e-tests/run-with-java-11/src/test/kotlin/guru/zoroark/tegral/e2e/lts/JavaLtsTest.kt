package guru.zoroark.tegral.e2e.lts

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.get
import kotlin.test.Test
import kotlin.test.assertEquals

class JavaLtsTest {
    class SomeComponent {
        fun sayHello() = "Hello!"
    }

    // This is just a dumb test that uses APIs from Tegral libs.
    @Test
    fun `Simple DI test`() {
        val env = tegralDi {
            put(::SomeComponent)
        }
        val component = env.get<SomeComponent>()
        assertEquals("Hello!", component.sayHello())
    }
}
