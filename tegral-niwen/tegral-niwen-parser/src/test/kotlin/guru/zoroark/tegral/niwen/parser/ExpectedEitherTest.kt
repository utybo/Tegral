package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.matchers.anyOf
import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.expectations.EitherBranch
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedEither
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedToken
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpectedEitherTest {

    sealed class BranchOneOrTwo {
        data class BranchOne(val oneWord: String) : BranchOneOrTwo() {
            companion object : ParserNodeDeclaration<BranchOne> by reflective()
        }

        data class BranchTwo(val twoWord: String) : BranchOneOrTwo() {
            companion object : ParserNodeDeclaration<BranchTwo> by reflective()
        }
    }

    private inline fun <reified R> rootKey(name: String): NodeParameterKey<Nothing, R> {
        return NodeParameterKey(typeOf<R>(), name)
    }

    private fun match(string: String): ExpectationResult<Nothing> {
        val tWord = tokenType("tWord")
        val tPar = tokenType("tPar")
        val tOne = tokenType("tOne")
        val tTwo = tokenType("tTwo")
        val lexer = niwenLexer {
            state {
                matches("1") isToken tOne
                matches("2") isToken tTwo
                matches("\\w+") isToken tWord
                anyOf("(", ")") isToken tPar
                " ".ignore
            }
        }
        val expectedEither = ExpectedEither<Nothing>(
            listOf(
                EitherBranch(
                    listOf(
                        ExpectedToken(tPar),
                        ExpectedNode(
                            BranchOneOrTwo.BranchOne,
                            rootKey<BranchOneOrTwo>("ambiguous")
                        ),
                        ExpectedToken(tPar)
                    )
                ),
                EitherBranch(
                    listOf(
                        ExpectedToken(tPar),
                        ExpectedNode(
                            BranchOneOrTwo.BranchTwo,
                            rootKey<BranchOneOrTwo>("ambiguous")
                        ),
                        ExpectedToken(tPar)
                    )
                )
            )
        )
        return expectedEither.matches(
            ParsingContext(
                lexer.tokenize(string),
                mapOf(
                    BranchOneOrTwo.BranchOne to DescribedType(
                        BranchOneOrTwo.BranchOne,
                        listOf(
                            ExpectedToken(tWord, storeValueIn = NodeParameterKey(typeOf<String>(), "oneWord")),
                            ExpectedToken(tOne)
                        )
                    ),
                    BranchOneOrTwo.BranchTwo to DescribedType(
                        BranchOneOrTwo.BranchTwo,
                        listOf(
                            ExpectedToken(tWord, storeValueIn = NodeParameterKey(typeOf<String>(), "twoWord")),
                            ExpectedToken(tTwo)
                        )
                    )
                )
            ),
            0
        )
    }

    @Test
    fun either_matches_first_branch() {
        val result = match("(hello 1)")

        assertTrue(result is ExpectationResult.Success, "Failed to parse: ${(result as? ExpectationResult.DidNotMatch)?.message}")
        assertEquals(
            BranchOneOrTwo.BranchOne("hello"),
            result.stored[rootKey<BranchOneOrTwo>("ambiguous")]
        )
    }

    @Test
    fun either_matches_second_branch() {
        val result = match("(hey 2)")
        assertTrue(result is ExpectationResult.Success, "Failed to parse: ${(result as? ExpectationResult.DidNotMatch)?.message}")
        assertEquals(
            BranchOneOrTwo.BranchTwo("hey"),
            result.stored[rootKey<BranchOneOrTwo>("ambiguous")]
        )
    }
}