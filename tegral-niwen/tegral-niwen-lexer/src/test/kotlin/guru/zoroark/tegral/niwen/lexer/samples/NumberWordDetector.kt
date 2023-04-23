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

package guru.zoroark.tegral.niwen.lexer.samples

import guru.zoroark.tegral.niwen.lexer.TokenType
import guru.zoroark.tegral.niwen.lexer.matchers.anyOf
import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.matchers.repeated
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import kotlin.test.Test

enum class Tokens : TokenType {
    WORD, NUMBER, WHITESPACE
}

val lexer = niwenLexer {
    state {
        matches("\\d+") isToken Tokens.NUMBER
        matches("\\w+") isToken Tokens.WORD
        anyOf(" ", "\n", "\t").repeated isToken Tokens.WHITESPACE
    }
}

class WordNumberDetectorTest {
    @Test
    fun test_my_lexer() {
        val result = lexer.tokenize(
            """
            Hello 42 World
            I hope you are having a good time
            """.trimIndent()
        )
        result.forEach {
            println("${it.string} --> ${it.tokenType}")
        }
    }
}
