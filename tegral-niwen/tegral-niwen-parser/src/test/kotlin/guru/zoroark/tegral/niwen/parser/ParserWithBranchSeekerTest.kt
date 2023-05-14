/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.optional
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
                expect(letterToken, withValue = "b") storeIn Letters::letterTwo
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
            - expect(GenericTokenType[letterToken], withValue = 'b'): "✅ Token 'b' is of correct type GenericTokenType[letterToken] and has correct 'withValue'"
              Stored:
                letterTwo (kotlin.String): b

            """.trimIndent(),
            result.debuggerResult
        )
    }

    @Test
    fun `Optional things`() {
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
                optional { expect(spaceToken) }
                expect(letterToken) storeIn Letters::letterOne
                optional { expect(letterToken, withValue = "c") }
                expect(letterToken) storeIn Letters::letterTwo
                optional { expect(spaceToken) }
            }
        }
        val result = parser.parseWithDebugger(lexer.tokenize(" ab"))
        assertIs<NiwenParser.ParserResult.Success<Letters>>(result)
        assertEquals(Letters("a", "b"), result.result)
        assertEquals(
            """
            ---
            Root: ✅ Parsing successful
            Stored:
              Parser root result (kotlin.Any?): "Letters(letterOne=a, letterTwo=b)"
            Expectations:
            - optional { 1 expectation(s) }: ✅ Took optional branch
              Expectations:
              - expect(GenericTokenType[space]): "✅ Token ' ' is of correct type GenericTokenType[space]"
            - expect(GenericTokenType[letterToken]): "✅ Token 'a' is of correct type GenericTokenType[letterToken]"
              Stored:
                letterOne (kotlin.String): a
            - optional { 1 expectation(s) }: ✅ Optional branch did not match and was skipped
              Expectations:
              - expect(GenericTokenType[letterToken], withValue = 'c'): "❌ At index 2, expected token of type GenericTokenType[letterToken] with value 'c', but encountered GenericTokenType[letterToken] ('b')"
            - expect(GenericTokenType[letterToken]): "✅ Token 'b' is of correct type GenericTokenType[letterToken]"
              Stored:
                letterTwo (kotlin.String): b
            - optional { 1 expectation(s) }: "✅ End of input reached: optional branch was skipped"

            """.trimIndent(),
            result.debuggerResult
        )
    }
}
