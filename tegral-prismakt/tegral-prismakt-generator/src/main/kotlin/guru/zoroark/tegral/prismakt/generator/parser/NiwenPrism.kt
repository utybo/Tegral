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

package guru.zoroark.tegral.prismakt.generator.parser

import guru.zoroark.tegral.niwen.lexer.TokenType
import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.stateLabel
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.TypeDescription
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.reflective

/**
 * ### What is NiwenPrism?
 *
 * NiwenPrism is an internal, **extremely** hacky parser for Prisma schema file. The main objective is to retrieve `@db.xyz` attributes.
 *
 * Prisma does not expose all attributes through its DMMF (the model passed to generators like PrismaKT), so we have to resort to this to
 * retrieve proper types.
 *
 * [Related Prisma issue](https://github.com/prisma/prisma/issues/10252)
 */
object NiwenPrism {
    private enum class Tokens : TokenType {
        WHITESPACE,
        NEWLINE,
        WORD,
        NUMBER,
        QUESTION_MARK,
        PAR_OPEN,
        PAR_CLOSE,
        SQRBRK_OPEN,
        SQRBRK_CLOSE,
        BRACE_OPEN,
        BRACE_CLOSE,
        AT_SIGN,
        DOT,
        COMMA,
        COLON,
        QUOTATION_MARK,
        EQUAL,
        STRING_CONTENT,
        ESCAPE_SEQUENCE
    }

    private val inStringState = stateLabel()

    val lexer = niwenLexer {
        default state {
            matches("[ \\t]+") isToken Tokens.WHITESPACE
            matches("[\\r\\n]+") isToken Tokens.NEWLINE
            matches("[a-zA-Z0-9_]+") isToken Tokens.WORD
            matches("[0-9]+") isToken Tokens.NUMBER
            '?' isToken Tokens.QUESTION_MARK
            '[' isToken Tokens.SQRBRK_OPEN
            ']' isToken Tokens.SQRBRK_CLOSE
            '(' isToken Tokens.PAR_OPEN
            ')' isToken Tokens.PAR_CLOSE
            '{' isToken Tokens.BRACE_OPEN
            '}' isToken Tokens.BRACE_CLOSE
            '@' isToken Tokens.AT_SIGN
            '.' isToken Tokens.DOT
            ',' isToken Tokens.COMMA
            ':' isToken Tokens.COLON
            '=' isToken Tokens.EQUAL
            '"' isToken Tokens.QUOTATION_MARK thenState inStringState
        }

        inStringState state {
            '"' isToken Tokens.QUOTATION_MARK thenState default
            matches("\\.") isToken Tokens.ESCAPE_SEQUENCE
            matches("""[^\\"\n\r]""") isToken Tokens.STRING_CONTENT
        }
    }

    val parser = niwenParser {
        PRoot {
            expect(Tokens.WORD, withValue = "id") storeIn
        }
    }

    data class PRoot(
        val datasources: List<PDatasource>,
        val generators: List<PGenerator>,
        val models: List<PModel>
    ) {
        companion object : ParserNodeDeclaration<PRoot> by reflective()
    }

    data class PDatasource(
        val provider: String,
        val url: PStringOrEnv,
        val extra: Map<String, Any>
    ) {
        companion object : ParserNodeDeclaration<PDatasource> {
            override fun make(args: TypeDescription): PDatasource {
                TODO("Not yet implemented")
            }
        }
    }

    data class PGenerator(
        val provider: String,
        val extra: Map<String, Any>
    ) {
        companion object : ParserNodeDeclaration<PGenerator> {
            override fun make(args: TypeDescription): PGenerator {
                TODO("Not yet implemented")
            }
        }
    }

    sealed class PStringOrEnv {
        data class PEnv(val envVariableName: String) {
            companion object : ParserNodeDeclaration<PEnv> by reflective()
        }

        data class PString(val value: String) {
            companion object : ParserNodeDeclaration<PString> by reflective()
        }
    }
}