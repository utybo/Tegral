package guru.zoroark.tegral.core

import kotlin.test.Test
import kotlin.test.assertSame

class BuildableTest {
    @Test
    fun `Buildable of returns same instance`() {
        val someObject = Any()
        val buildable = Buildable.of(someObject)

        assertSame(someObject, buildable.build())
    }
}
