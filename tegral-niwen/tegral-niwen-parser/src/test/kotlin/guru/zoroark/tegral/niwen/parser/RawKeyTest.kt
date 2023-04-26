package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.matchers.repeated
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.item
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.repeated
import guru.zoroark.tegral.niwen.parser.expectations.RawKey
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

private fun makeRawKey() = RawKey(typeOf<List<String>>(), "chars")

class RawKeyTest {

    data class Word(val characters: List<String>) {
        companion object : ParserNodeDeclaration<Word> by reflective()
    }

    @Test
    fun `Store in raw key then retrieve`() {
        val tChar = tokenType("character")
        val lexer = niwenLexer {
            state {
                'a'..'z' isToken tChar
            }
        }
        val parser = niwenParser<Word> {
            Word root {
                repeated {
                    expect(tChar) storeIn item
                } storeIn RawKey(typeOf<List<String>>(), "characters")
            }
        }
        val word = parser.parse(lexer.tokenize("abcd"))
        assertEquals(Word(listOf("a", "b", "c", "d")), word)
    }
}
