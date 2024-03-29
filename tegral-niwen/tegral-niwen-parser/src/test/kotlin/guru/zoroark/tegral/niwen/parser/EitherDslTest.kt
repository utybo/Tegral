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

import guru.zoroark.tegral.niwen.lexer.matchers.anyOf
import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.matchers.repeated
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.either
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.or
import guru.zoroark.tegral.niwen.parser.dsl.self
import guru.zoroark.tegral.niwen.parser.dsl.subtype
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

sealed class Expression {
    companion object : ParserNodeDeclaration<Expression> by subtype()
}

data class Operation(
    val leftOperand: Expression,
    val operator: String,
    val rightOperand: Expression
) : Expression() {
    companion object : ParserNodeDeclaration<Operation> by reflective()
}

data class Number(
    val value: String
) : Expression() {
    companion object : ParserNodeDeclaration<Number> by reflective()
}

class EitherDslTest {
    @Test
    fun test_either_dsl() {
        // Operation parser
        val tOperator = tokenType("tOperator")
        val tNumber = tokenType("tNumber")
        val tOpenPar = tokenType("tOpenPar")
        val tClosePar = tokenType("tClosePar")
        val lexer = niwenLexer {
            state {
                anyOf("+", "-", "/", "*") isToken tOperator
                ('0'..'9').repeated isToken tNumber
                "(" isToken tOpenPar
                ")" isToken tClosePar
            }
        }
        val parser = niwenParser {
            Expression root {
                either {
                    expect(Number) storeIn self()
                } or {
                    expect(tOpenPar)
                    expect(Operation) storeIn self()
                    expect(tClosePar)
                }
            }
            Number {
                expect(tNumber) storeIn Number::value
            }
            Operation {
                expect(Expression) storeIn Operation::leftOperand
                expect(tOperator) storeIn Operation::operator
                expect(Expression) storeIn Operation::rightOperand
            }
        }
        val str = "((3+4)/(1+(3-1)))"
        assertEquals(
            Operation(
                Operation(
                    Number("3"),
                    "+",
                    Number("4")
                ),
                "/",
                Operation(
                    Number("1"),
                    "+",
                    Operation(
                        Number("3"),
                        "-",
                        Number("1")
                    )
                )
            ),
            parser.parse(lexer.tokenize(str))
        )
    }

    @Test
    fun test_either_dsl_short_notation() {
        // Operation parser

        val tOperator = tokenType("tOperator")
        val tNumber = tokenType("tNumber")
        val tOpenPar = tokenType("tOpenPar")
        val tClosePar = tokenType("tClosePar")
        val lexer = niwenLexer {
            state {
                anyOf("+", "-", "/", "*") isToken tOperator
                ('0'..'9').repeated isToken tNumber
                "(" isToken tOpenPar
                ")" isToken tClosePar
            }
        }
        val parser = niwenParser {
            Expression root {
                either {
                    +Number storeIn self()
                } or {
                    +tOpenPar
                    +Operation storeIn self()
                    +tClosePar
                }
            }
            Number {
                +tNumber storeIn Number::value
            }
            Operation {
                +Expression storeIn Operation::leftOperand
                +tOperator storeIn Operation::operator
                +Expression storeIn Operation::rightOperand
            }
        }
        val str = "((3+4)/(1+(3-1)))"
        assertEquals(
            Operation(
                Operation(
                    Number("3"),
                    "+",
                    Number("4")
                ),
                "/",
                Operation(
                    Number("1"),
                    "+",
                    Operation(
                        Number("3"),
                        "-",
                        Number("1")
                    )
                )
            ),
            parser.parse(lexer.tokenize(str))
        )
    }

    @Test
    fun `Test either that does not match any`() {
        val token = tokenType()
        val lexer = niwenLexer {
            state { 'a'..'z' isToken token }
        }
        val parser = niwenParser {
            Number root {
                either {
                    expect(token, withValue = "a")
                } or {
                    expect(token, withValue = "b")
                }
            }
        }
        val exc = assertFailsWith<NiwenParserException> { parser.parse(lexer.tokenize("ccdd")) }
        assertTrue { exc.message!!.contains("None of the 2 branches matched.") }
    }

    sealed class Value {
        companion object : ParserNodeDeclaration<Value> by subtype()

        data class Number(val number: String) : Value() {
            companion object : ParserNodeDeclaration<Number> by reflective()
        }

        data class Str(val string: String) : Value() {
            companion object : ParserNodeDeclaration<Str> by reflective()
        }
    }

    @Test
    fun `by subtype with either`() {
        val tNumber = tokenType("tNumber")
        val tStr = tokenType("tStr")
        val lexer = niwenLexer {
            state {
                matches("[a-z]+") isToken tStr
                matches("[0-9]+") isToken tNumber
            }
        }
        val parser = niwenParser {
            Value root {
                either {
                    expect(Value.Number) storeIn self()
                } or {
                    expect(Value.Str) storeIn self()
                }
            }

            Value.Number {
                expect(tNumber) storeIn Value.Number::number
            }

            Value.Str {
                expect(tStr) storeIn Value.Str::string
            }
        }
        val result = parser.parse(lexer.tokenize("123"))
        assertEquals(Value.Number("123"), result)
    }
}
