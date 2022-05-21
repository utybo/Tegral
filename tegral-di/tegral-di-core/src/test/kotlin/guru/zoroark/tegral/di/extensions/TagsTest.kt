package guru.zoroark.tegral.di.extensions

import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.Identifier
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TagsTest {
    private fun makeDeclaration() = Declaration(Identifier(String::class)) { "Hello!" }

    object TagOne : DeclarationTag
    object TagTwo : DeclarationTag
    object TagThree : DeclarationTag

    @Test
    fun `with single tag`() {
        val declaration = makeDeclaration()
        declaration with TagOne
        assertEquals<List<DeclarationTag>>(listOf(TagOne), declaration.tags)
    }

    @Test
    fun `with tag list`() {
        val declaration = makeDeclaration()
        declaration with listOf(TagOne, TagTwo)
        assertEquals<List<DeclarationTag>>(listOf(TagOne, TagTwo), declaration.tags)
    }

    @Test
    fun `with tag plus tag`() {
        val declaration = makeDeclaration()
        declaration with TagOne + TagTwo
        assertEquals<List<DeclarationTag>>(listOf(TagOne, TagTwo), declaration.tags)
    }

    @Test
    fun `with tag plus tag plus tag`() {
        val declaration = makeDeclaration()
        declaration with TagOne + TagTwo + TagThree
        assertEquals<List<DeclarationTag>>(listOf(TagOne, TagTwo, TagThree), declaration.tags)
    }
}
