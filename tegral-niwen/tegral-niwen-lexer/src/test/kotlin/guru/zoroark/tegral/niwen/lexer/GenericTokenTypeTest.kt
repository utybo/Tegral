package guru.zoroark.tegral.niwen.lexer

import kotlin.test.Test
import kotlin.test.assertEquals

class GenericTokenTypeTest {
    @Test
    fun `toString call`() {
        assertEquals("GenericTokenType[myName]", tokenType("myName").toString())
    }

    @Test
    fun `getName call`() {
        assertEquals("myName", tokenType("myName").name)
    }
}
