package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.matchers.repeated
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.either
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.item
import guru.zoroark.tegral.niwen.parser.dsl.lookahead
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.or
import guru.zoroark.tegral.niwen.parser.dsl.repeated
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LookaheadTest {
    data class Sentence(val words: List<Word>, val finalWord: Word) {
        companion object : ParserNodeDeclaration<Sentence> by reflective()
    }

    data class Word(val word: String) {
        companion object : ParserNodeDeclaration<Word> by reflective()
    }

    @Test
    fun `Empty lookahead`() {
        val tWord = tokenType()
        val tDot = tokenType()
        val lexer = niwenLexer {
            state {
                matches("[a-zA-Z]+") isToken tWord
                '.' isToken tDot
                ' '.ignore
            }
        }

        val parser = niwenParser<Sentence> {
            Sentence root {
                repeated {
                    expect(Word) storeIn item
                    lookahead {
                        expect(Word)
                    }
                } storeIn Sentence::words

                expect(Word) storeIn Sentence::finalWord
                expect(tDot)
            }

            Word {
                expect(tWord) storeIn Word::word
            }
        }

        val result = parser.parse(lexer.tokenize("This is some words."))
        assertEquals(Sentence(listOf(Word("This"), Word("is"), Word("some")), Word("words")), result)
    }
}
