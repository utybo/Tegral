package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.matchers.anyOf
import guru.zoroark.tegral.niwen.lexer.matchers.repeated
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.dsl.ExpectationReceiver
import guru.zoroark.tegral.niwen.parser.dsl.either
import guru.zoroark.tegral.niwen.parser.dsl.expect
import guru.zoroark.tegral.niwen.parser.dsl.niwenParser
import guru.zoroark.tegral.niwen.parser.dsl.or
import guru.zoroark.tegral.niwen.parser.dsl.self
import guru.zoroark.tegral.niwen.parser.dsl.subtype
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.asKey
import guru.zoroark.tegral.niwen.parser.expectations.key
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals


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
}