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

import guru.zoroark.tegral.niwen.lexer.Token
import guru.zoroark.tegral.niwen.lexer.TokenType
import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.matchers.repeated
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.stateLabel
import guru.zoroark.tegral.niwen.parser.NiwenParser
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.TypeDescription
import guru.zoroark.tegral.niwen.parser.dsl.either
import guru.zoroark.tegral.niwen.parser.dsl.emit
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.item
import guru.zoroark.tegral.niwen.parser.dsl.lookahead
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.optional
import guru.zoroark.tegral.niwen.parser.dsl.or
import guru.zoroark.tegral.niwen.parser.dsl.repeated
import guru.zoroark.tegral.niwen.parser.dsl.self
import guru.zoroark.tegral.niwen.parser.dsl.subtype
import guru.zoroark.tegral.niwen.parser.expectations.key
import guru.zoroark.tegral.niwen.parser.reflective
import javax.swing.text.html.parser.Parser

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
    fun tokenize(str: String): List<Token> {
        return lexer.tokenize(str)
    }

    fun parse(tokens: List<Token>): PRoot {
        return parser.parse(tokens)
    }

    fun parseDebug(tokens: List<Token>): NiwenParser.ParserResult<PRoot> {
        return parser.parseWithDebugger(tokens)
    }
}

private enum class Tokens : TokenType {
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
private val inStringEscapeState = stateLabel()

val lexer = niwenLexer {
    default state {
        matches("[ \\t]+").ignore
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
        '\\'.ignore thenState inStringEscapeState
        matches("""[^\\"\n\r]+""") isToken Tokens.STRING_CONTENT
    }

    inStringEscapeState state {
        matches(".") isToken Tokens.STRING_CONTENT thenState inStringState
    }
}

// TODO fill the stopReason EVERYWHERE

val parser = niwenParser<PRoot> {
    PRoot root {
        repeated { expect(PRootElement) storeIn item } storeIn PRoot::elements
        optional { expect(Tokens.NEWLINE) }
    }

    PRootElement {
        optional { expect(Tokens.NEWLINE) }
        either {
            expect(PDatasource) storeIn self()
        } or {
            expect(PGenerator) storeIn self()
        } or {
            expect(PModel) storeIn self()
        } or {
            expect(PEnum) storeIn self()
        }
        optional { expect(Tokens.NEWLINE) }
    }

    "Datasource/generator" group {
        PDatasource {
            expect(Tokens.WORD, "datasource")
            expect(Tokens.WORD) storeIn PDatasource::name
            expect(Tokens.BRACE_OPEN)
            optional { expect(Tokens.NEWLINE) }
            expect(PPropertySet) storeIn PDatasource::properties
            expect(Tokens.BRACE_CLOSE)
        }

        PGenerator {
            expect(Tokens.WORD, "generator")
            expect(Tokens.WORD) storeIn PGenerator::name
            expect(Tokens.BRACE_OPEN)
            optional { expect(Tokens.NEWLINE) }
            expect(PPropertySet) storeIn PGenerator::properties
            expect(Tokens.BRACE_CLOSE)
        }
    }

    PModel {
        expect(Tokens.WORD, "model")
        expect(Tokens.WORD) storeIn PModel::name
        expect(Tokens.BRACE_OPEN)
        optional { expect(Tokens.NEWLINE) }
        repeated { expect(PField) storeIn item } storeIn PModel::fields
        expect(Tokens.BRACE_CLOSE)
    }

    PField {
        expect(Tokens.WORD) storeIn PField::name
        expect(Tokens.WORD) storeIn PField::type

        either {
            expect(Tokens.SQRBRK_OPEN)
            expect(Tokens.SQRBRK_CLOSE)
            emit(true) storeIn PField::isArray
        } or {
            emit(false) storeIn PField::isArray
        }

        either {
            expect(Tokens.QUESTION_MARK)
            emit(true) storeIn PField::isOptional
        } or {
            emit(false) storeIn PField::isOptional
        }

        repeated { expect(PAttribute) storeIn item } storeIn PField::attributes

        expect(Tokens.NEWLINE)
    }

    PAttribute {
        expect(Tokens.AT_SIGN)
        either {
            expect(Tokens.AT_SIGN)
            emit(true) storeIn PAttribute::isBlockAttribute
        } or {
            emit(false) storeIn PAttribute::isBlockAttribute
        }

        either {
            expect(Tokens.WORD, "db")
            expect(Tokens.DOT)
            expect(Tokens.WORD) transform { "db.${it}" } storeIn PAttribute::name
        } or {
            expect(Tokens.WORD) storeIn PAttribute::name
        }
        optional {
            expect(PArgsList) transform { it.args } storeIn PAttribute::params
        }
    }

    "Expression" group {
        PExpression {
            either {
                expect(PExpValue) storeIn self()
            } or {
                expect(PExpFunCall) storeIn self()
            } or {
                expect(PExpArray) storeIn self()
            } or {
                expect(PExpReference) storeIn self()
            }
        }

        PExpValue {
            expect(PString) storeIn PExpValue::value
        }

        PExpFunCall {
            expect(Tokens.WORD) storeIn PExpFunCall::functionName
            expect(PArgsList) transform { it.args } storeIn PExpFunCall::argsList
        }

        PExpReference {
            expect(Tokens.WORD) storeIn PExpReference::ref
        }

        PExpArray {
            expect(Tokens.SQRBRK_OPEN)
            repeated {
                expect(PExpression) storeIn item
                either {
                    lookahead { expect(Tokens.SQRBRK_CLOSE) }
                } or {
                    expect(Tokens.COMMA)
                }
            } storeIn PExpArray::values
            expect(Tokens.SQRBRK_CLOSE)
        }
    }

    "Argument" group {
        PArgsList {
            expect(Tokens.PAR_OPEN)
            repeated {
                expect(PArgument) storeIn item
                either {
                    lookahead { expect(Tokens.PAR_CLOSE) }
                } or {
                    expect(Tokens.COMMA)
                }
            } storeIn PArgsList::args
            expect(Tokens.PAR_CLOSE)
        }

        PArgument {
            either {
                expect(PArgNamed) storeIn self()
            } or {
                expect(PArgBare) storeIn self()
            }
        }

        PArgNamed {
            expect(Tokens.WORD) storeIn PArgNamed::name
            expect(Tokens.COLON)
            expect(PExpression) storeIn PArgNamed::expr
        }

        PArgBare {
            expect(PExpression) storeIn PArgBare::expr
        }
    }

    "Properties" group {
        PPropertySet {
            repeated { expect(PProperty) storeIn item } storeIn PPropertySet::properties
        }

        PProperty {
            expect(Tokens.WORD) storeIn PProperty::name
            expect(Tokens.EQUAL)
            expect(PValueOrEnv) storeIn PProperty::value
            expect(Tokens.NEWLINE)
        }

        PValueOrEnv {
            either {
                expect(PValue) storeIn self()
            } or {
                expect(PEnv) storeIn self()
            }
        }

        PValue {
            expect(PString) storeIn PValue::value
        }

        PEnv {
            expect(Tokens.WORD, "env") // TODO case-insensitive
            expect(Tokens.PAR_OPEN)
            expect(PString) storeIn PEnv::env
            expect(Tokens.PAR_CLOSE)
        }

        PString {
            expect(Tokens.QUOTATION_MARK)
            repeated { expect(Tokens.STRING_CONTENT) storeIn item } storeIn PString.parts
            expect(Tokens.QUOTATION_MARK)
        }
    }

    PEnum {
        expect(Tokens.WORD, withValue = "enum")
        expect(Tokens.WORD) storeIn PEnum::enumName
        expect(Tokens.BRACE_OPEN)
        optional { expect(Tokens.NEWLINE) }
        repeated {
            expect(Tokens.WORD) storeIn item
            expect(Tokens.NEWLINE)
        } storeIn PEnum::enumTypes
        expect(Tokens.BRACE_CLOSE)
    }
}

data class PRoot(
    val elements: List<PRootElement>
) {
    companion object : ParserNodeDeclaration<PRoot> by reflective()
}

sealed class PRootElement {
    companion object : ParserNodeDeclaration<PRootElement> by subtype()
}

data class PDatasource(val name: String, val properties: PPropertySet) : PRootElement() {
    companion object : ParserNodeDeclaration<PDatasource> by reflective()
}

data class PPropertySet(val properties: List<PProperty>) {
    companion object : ParserNodeDeclaration<PPropertySet> by reflective()
}

data class PProperty(val name: String, val value: PValueOrEnv) {
    companion object : ParserNodeDeclaration<PProperty> by reflective()
}

sealed class PValueOrEnv {
    companion object : ParserNodeDeclaration<PValueOrEnv> by subtype()
}

data class PValue(val value: PString) : PValueOrEnv() {
    companion object : ParserNodeDeclaration<PValue> by reflective()
}

data class PEnv(val env: PString) : PValueOrEnv() {
    companion object : ParserNodeDeclaration<PEnv> by reflective()
}

data class PString(val string: String) {

    companion object : ParserNodeDeclaration<PString> {
        val parts = key<PString, List<String>>("parts")
        override fun make(args: TypeDescription<PString>): PString {
            return PString(args[parts].joinToString(""))
        }
    }
}

data class PGenerator(val name: String, val properties: PPropertySet) : PRootElement() {
    companion object : ParserNodeDeclaration<PGenerator> by reflective()
}

data class PModel(val name: String, val fields: List<PField>) : PRootElement() {
    companion object : ParserNodeDeclaration<PModel> by reflective()
}

data class PField(val name: String, val type: String, val isOptional: Boolean, val isArray: Boolean, val attributes: List<PAttribute>) {
    companion object : ParserNodeDeclaration<PField> by reflective()
}

data class PAttribute(val name: String, val params: List<PArgument> = listOf(), val isBlockAttribute: Boolean) {
    companion object : ParserNodeDeclaration<PAttribute> by reflective()
}

data class PArgsList(val args: List<PArgument>) {
    companion object : ParserNodeDeclaration<PArgsList> by reflective()
}

sealed class PArgument {
    companion object : ParserNodeDeclaration<PArgument> by subtype()
}

data class PArgNamed(val name: String, val expr: PExpression) : PArgument() {
    companion object : ParserNodeDeclaration<PArgNamed> by reflective()
}

data class PArgBare(val expr: PExpression) : PArgument() {
    companion object : ParserNodeDeclaration<PArgBare> by reflective()
}

sealed class PExpression {
    companion object : ParserNodeDeclaration<PExpression> by subtype()


}

data class PExpValue(val value: PString) : PExpression() {
    companion object : ParserNodeDeclaration<PExpValue> by reflective()
}

data class PExpFunCall(val functionName: String, val argsList: List<PArgument>) : PExpression() {
    companion object : ParserNodeDeclaration<PExpFunCall> by reflective()
}

data class PExpReference(val ref: String) : PExpression() {
    companion object : ParserNodeDeclaration<PExpReference> by reflective()
}

data class PExpArray(val values: List<PExpression>) : PExpression() {
    companion object : ParserNodeDeclaration<PExpArray> by reflective()
}

data class PEnum(val enumName: String, val enumTypes: List<String>) : PRootElement() {
    companion object : ParserNodeDeclaration<PEnum> by reflective()
}
