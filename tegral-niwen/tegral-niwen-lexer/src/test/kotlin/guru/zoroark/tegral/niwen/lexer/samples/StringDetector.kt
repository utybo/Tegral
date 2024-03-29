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

import guru.zoroark.tegral.niwen.lexer.StateLabel
import guru.zoroark.tegral.niwen.lexer.TokenType
import guru.zoroark.tegral.niwen.lexer.matchers.anyOf
import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import kotlin.test.Test

class StringDetector {
    enum class Tokens : TokenType {
        WORD, WHITESPACE, QUOTES, STRING_CONTENT, PUNCTUATION
    }

    enum class States : StateLabel {
        IN_STRING
    }

    @Test
    fun string_detector() {
        val lexer = niwenLexer {
            default state {
                anyOf(" ", "\t", "\n") isToken Tokens.WHITESPACE
                "\"" isToken Tokens.QUOTES thenState States.IN_STRING
                anyOf(".", ",", "!", "?") isToken Tokens.PUNCTUATION
                matches("\\w+") isToken Tokens.WORD
            }
            States.IN_STRING state {
                matches("""(\\"|[^"])+""") isToken Tokens.STRING_CONTENT
                "\"" isToken Tokens.QUOTES thenState default
            }
        }
        val str = """
            Hello Kotlin! "This is \" Pretty Cool" Heyo!
        """.trimIndent()
        val tokens = lexer.tokenize(str)
        tokens.forEach { println(it.string + " --> " + it.tokenType) }
    }
}
