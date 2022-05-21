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
