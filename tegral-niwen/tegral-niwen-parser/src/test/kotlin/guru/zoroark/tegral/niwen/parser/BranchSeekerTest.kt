package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ParserWithBranchSeekerTest {
    data class Letters(val letterOne: String, val letterTwo: String) {
        companion object : ParserNodeDeclaration<Letters> by reflective()
    }

    @Test
    fun `Simple parsing example with debugging (branch seeker) enabled`() {
        val letterToken = tokenType("letterToken")
        val spaceToken = tokenType("space")
        val lexer = niwenLexer {
            default state {
                'a'..'z' isToken letterToken
                ' ' isToken spaceToken
            }
        }
        val parser = niwenParser {
            Letters root {
                expect(letterToken) storeIn Letters::letterOne
                expect(spaceToken)
                expect(letterToken) storeIn Letters::letterTwo
            }
        }
        val result = parser.parseWithDebugger(lexer.tokenize("a b"))
        assertIs<NiwenParser.ParserResult.Success<Letters>>(result)
        assertEquals(Letters("a", "b"), result.result)
        assertEquals(
            """
            ---
            Root: ✅ Parsing successful
            Stored:
              Parser root result (kotlin.Any?): "Letters(letterOne=a, letterTwo=b)"
            Expectations:
            - expect(GenericTokenType[letterToken]): "✅ Token 'a' is of correct type GenericTokenType[letterToken]"
              Stored:
                letterOne (kotlin.String): a
            - expect(GenericTokenType[space]): "✅ Token ' ' is of correct type GenericTokenType[space]"
            - expect(GenericTokenType[letterToken]): "✅ Token 'b' is of correct type GenericTokenType[letterToken]"
              Stored:
                letterTwo (kotlin.String): b

            """.trimIndent(),
            result.debuggerResult
        )
    }
}
