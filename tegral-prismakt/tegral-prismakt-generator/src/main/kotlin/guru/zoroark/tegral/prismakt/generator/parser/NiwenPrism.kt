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
import guru.zoroark.tegral.niwen.parser.dsl.repeatedIgnore
import guru.zoroark.tegral.niwen.parser.dsl.self
import guru.zoroark.tegral.niwen.parser.dsl.subtype
import guru.zoroark.tegral.niwen.parser.expectations.key
import guru.zoroark.tegral.niwen.parser.reflective

/**
 * An internal, **extremely** hacky parser for Prisma schema file. The main objective is to retrieve `@db.xyz`
 * attributes.
 *
 * Prisma does not expose all attributes through its DMMF (the model passed to generators like PrismaKT), so we have to
 * resort to this to retrieve proper types.
 *
 * [Related Prisma issue](https://github.com/prisma/prisma/issues/10252)
 */
object NiwenPrism {
    /**
     * Tokenize the provided string using the Niwen Prism parser.
     *
     * This sequence of tokens can then be provided to [parse] or [parseDebug].
     */
    fun tokenize(str: String): List<Token> {
        return lexer.tokenize(str)
    }

    /**
     * Parse a list of tokens to a parser result of [PRoot].
     *
     * This function will not provide additional debugging information, unlike [parseDebug].
     */
    fun parse(tokens: List<Token>): NiwenParser.ParserResult<PRoot> {
        return parser.parseToResult(tokens, false)
    }

    /**
     * Parse a list of tokens to a parser result of [PRoot], with debug information enabled.
     *
     * This is slower than using [parse] but allows seeing an exact breakdown of the parsing. Use it when necessary.
     */
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

private val lexer = niwenLexer {
    default state {
        matches("[ \\t]+").ignore
        matches("[\\r\\n]+") isToken Tokens.NEWLINE
        matches("[0-9]+") isToken Tokens.NUMBER
        matches("[a-zA-Z0-9_]+") isToken Tokens.WORD
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
        matches("//.+?(?=\\n)").ignore
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

private val parser = niwenParser<PRoot> {
    PRoot root {
        repeated { expect(PRootElement) storeIn item } storeIn PRoot::elements
        repeatedIgnore { expect(Tokens.NEWLINE) }
    }

    PRootElement {
        repeatedIgnore { expect(Tokens.NEWLINE) }
        either {
            expect(PDatasource) storeIn self()
        } or {
            expect(PGenerator) storeIn self()
        } or {
            expect(PModel) storeIn self()
        } or {
            expect(PEnum) storeIn self()
        }
        repeatedIgnore { expect(Tokens.NEWLINE) }
    }

    "Datasource/generator" group {
        PDatasource {
            expect(Tokens.WORD, "datasource")
            expect(Tokens.WORD) storeIn PDatasource::name
            expect(Tokens.BRACE_OPEN)
            repeatedIgnore { expect(Tokens.NEWLINE) }
            expect(PPropertySet) storeIn PDatasource::properties
            expect(Tokens.BRACE_CLOSE)
        }

        PGenerator {
            expect(Tokens.WORD, "generator")
            expect(Tokens.WORD) storeIn PGenerator::name
            expect(Tokens.BRACE_OPEN)
            repeatedIgnore { expect(Tokens.NEWLINE) }
            expect(PPropertySet) storeIn PGenerator::properties
            expect(Tokens.BRACE_CLOSE)
        }
    }

    PModel {
        expect(Tokens.WORD, "model")
        expect(Tokens.WORD) storeIn PModel::name
        expect(Tokens.BRACE_OPEN)
        repeatedIgnore { expect(Tokens.NEWLINE) }
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

        repeatedIgnore(min = 1) { expect(Tokens.NEWLINE) }
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
            expect(Tokens.WORD) transform { "db.$it" } storeIn PAttribute::name
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
            either {
                expect(PExpValueString) storeIn self()
            } or {
                expect(PExpValueInt) storeIn self()
            }
        }

        PExpValueString {
            expect(PString) storeIn PExpValueString::value
        }

        PExpValueInt {
            expect(Tokens.NUMBER) transform { it.toInt() } storeIn PExpValueInt::value
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
            repeatedIgnore(min = 1) { expect(Tokens.NEWLINE) }
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
        repeatedIgnore { expect(Tokens.NEWLINE) }
        repeated {
            expect(Tokens.WORD) storeIn item
            repeatedIgnore(min = 1) { expect(Tokens.NEWLINE) }
        } storeIn PEnum::enumTypes
        expect(Tokens.BRACE_CLOSE)
    }
}

/**
 * Root parsing result of [NiwenPrism]
 *
 * @property elements Root elements in this schema.
 */
data class PRoot(val elements: List<PRootElement>) {
    companion object : ParserNodeDeclaration<PRoot> by reflective()
}

/**
 * An element of the root of a Prisma schema document
 */
sealed class PRootElement {
    companion object : ParserNodeDeclaration<PRootElement> by subtype()
}

/**
 * A `datasource ... { }` element.
 *
 * @property name Name for this datasource
 * @property properties Properties set for this datasource
 */
data class PDatasource(val name: String, val properties: PPropertySet) : PRootElement() {
    companion object : ParserNodeDeclaration<PDatasource> by reflective()
}

/**
 * A set of [properties][PProperty]
 *
 * @property properties Properties contained within this property set
 */
data class PPropertySet(val properties: List<PProperty>) {
    companion object : ParserNodeDeclaration<PPropertySet> by reflective()
}

/**
 * A simple name to value-or-env binding.
 *
 * @property name Name of this property
 * @property value Value, or environment variable name for this value. See [PValueOrEnv] for more details.
 */
data class PProperty(val name: String, val value: PValueOrEnv) {
    companion object : ParserNodeDeclaration<PProperty> by reflective()
}

/**
 * Either a regular [PValue] string, or an environment variable reference ([PEnv]).
 */
sealed class PValueOrEnv {
    companion object : ParserNodeDeclaration<PValueOrEnv> by subtype()
}

/**
 * A regular [PString] value as part of a [PValueOrEnv]
 *
 * @property value Actual value
 */
data class PValue(val value: PString) : PValueOrEnv() {
    companion object : ParserNodeDeclaration<PValue> by reflective()
}

/**
 * An environment variable reference. Not substituted immediately.
 *
 * @property env Name of the environment variable to check
 */
data class PEnv(val env: PString) : PValueOrEnv() {
    companion object : ParserNodeDeclaration<PEnv> by reflective()
}

/**
 * A regular ol' string.
 *
 * @property string The value of the string. All escaping is already taken care of, this string can be used as-is.
 */
data class PString(val string: String) {
    companion object : ParserNodeDeclaration<PString> {
        /**
         * Key for the parts of a string
         */
        val parts = key<PString, List<String>>("parts")

        override fun make(args: TypeDescription<PString>): PString {
            return PString(args[parts].joinToString(""))
        }
    }
}

/**
 * A `generator ... { }` in a Prisma schema
 *
 * @property name Generator name
 * @property properties Properties of this generator
 */
data class PGenerator(val name: String, val properties: PPropertySet) : PRootElement() {
    companion object : ParserNodeDeclaration<PGenerator> by reflective()
}

/**
 * A `model ... { }` in a Prisma schema
 *
 * @property name Name of this model
 * @property fields Fields contained in this model
 */
data class PModel(val name: String, val fields: List<PField>) : PRootElement() {
    companion object : ParserNodeDeclaration<PModel> by reflective()
}

/**
 * A field within a [PModel]
 */
data class PField(
    /**
     * Name of this field
     */
    val name: String,

    /**
     * Type (as in, simple Prisma type) of this field.
     */
    val type: String,

    /**
     * True if the field is marked with a `?`, i.e. it is optional (nullable), false otherwise
     */
    val isOptional: Boolean,

    /**
     * True if the field is marked with a `[]`, i.e. it is an array, false otherwise.
     */
    val isArray: Boolean,

    /**
     * List of [attributes][PAttribute] associated with this field.
     */
    val attributes: List<PAttribute>
) {
    companion object : ParserNodeDeclaration<PField> by reflective()
}

/**
 * An attribute on a field, e.g. `@db.TinyBlob`.
 *
 * @property name Name of the attribute. This includes any namespace-like component, e.g. for `@db.TinyBlob(...)`, the
 * name will be `db.TinyBlob`
 * @property params List of arguments provided to this attribute within brackets `(...)`. Empty if none are provided.
 * @property isBlockAttribute True if the attribute has two at-signs (e.g. `@@id`), false otherwise (e.g. `@id`).
 */
data class PAttribute(val name: String, val params: List<PArgument> = listOf(), val isBlockAttribute: Boolean) {
    companion object : ParserNodeDeclaration<PAttribute> by reflective()
}

/**
 * A list of [arguments][PArgument]
 *
 * @property args The arguments
 */
data class PArgsList(val args: List<PArgument>) {
    companion object : ParserNodeDeclaration<PArgsList> by reflective()
}

/**
 * An argument.
 */
sealed class PArgument {
    companion object : ParserNodeDeclaration<PArgument> by subtype()
}

/**
 * A named argument, that is, an argument that has a name and an associated expression, e.g. `@something(foo = "bar")`.
 *
 * @property name Name given to this argument
 * @property expr Expression for this argument.
 */
data class PArgNamed(val name: String, val expr: PExpression) : PArgument() {
    companion object : ParserNodeDeclaration<PArgNamed> by reflective()
}

/**
 * A simple argument that only contains an [expression][PExpression]
 *
 * @property expr Expression for this argument.
 */
data class PArgBare(val expr: PExpression) : PArgument() {
    companion object : ParserNodeDeclaration<PArgBare> by reflective()
}

/**
 * An expression.
 */
sealed class PExpression {
    companion object : ParserNodeDeclaration<PExpression> by subtype()
}

/**
 * An [expression][PExpression] that is a simple value.
 */
sealed class PExpValue : PExpression() {
    companion object : ParserNodeDeclaration<PExpValue> by subtype()
}

/**
 * An [expression][PExpression] [value][PExpValue] that is a simple [string][PString].
 *
 * @property value The actual [PString] value of this expression.
 */
data class PExpValueString(val value: PString) : PExpValue() {
    companion object : ParserNodeDeclaration<PExpValueString> by reflective()
}

/**
 * An [expression][PExpression] [value][PExpValue] that is an integer value
 */
data class PExpValueInt(val value: Int) : PExpValue() {
    companion object : ParserNodeDeclaration<PExpValueInt> by reflective()
}

/**
 * An [expression][PExpression] that is a call to a function named [functionName], called with the provided [argsList].
 *
 * @property functionName Name of the function to call
 * @property argsList List of arguments to use when calling the function.
 */
data class PExpFunCall(val functionName: String, val argsList: List<PArgument>) : PExpression() {
    companion object : ParserNodeDeclaration<PExpFunCall> by reflective()
}

/**
 * An [expression][PExpression] that is a reference to something.
 *
 * @property ref The name of the reference
 */
data class PExpReference(val ref: String) : PExpression() {
    companion object : ParserNodeDeclaration<PExpReference> by reflective()
}

/**
 * An [expression][PExpression] that is an array of other expressions.
 *
 * @property values The expressions constituting this array.
 */
data class PExpArray(val values: List<PExpression>) : PExpression() {
    companion object : ParserNodeDeclaration<PExpArray> by reflective()
}

/**
 * An enumeration (`enum ... { }`) in a Prisma schema.
 *
 * @property enumName The name of this enumeration.
 * @property enumTypes Name of the values of this enumeration.
 */
data class PEnum(val enumName: String, val enumTypes: List<String>) : PRootElement() {
    companion object : ParserNodeDeclaration<PEnum> by reflective()
}
