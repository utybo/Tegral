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
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.expectations.EitherBranch
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedEither
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedToken
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.StoreStateCallback
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

    @Suppress("LongMethod")
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
                            StoreStateCallback(rootKey<BranchOneOrTwo>("ambiguous"))
                        ),
                        ExpectedToken(tPar)
                    )
                ),
                EitherBranch(
                    listOf(
                        ExpectedToken(tPar),
                        ExpectedNode(
                            BranchOneOrTwo.BranchTwo,
                            StoreStateCallback(rootKey<BranchOneOrTwo>("ambiguous"))
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
                            ExpectedToken(
                                tWord,
                                stateCallback = StoreStateCallback(NodeParameterKey(typeOf<String>(), "oneWord"))
                            ),
                            ExpectedToken(tOne)
                        )
                    ),
                    BranchOneOrTwo.BranchTwo to DescribedType(
                        BranchOneOrTwo.BranchTwo,
                        listOf(
                            ExpectedToken(
                                tWord,
                                stateCallback = StoreStateCallback(NodeParameterKey(typeOf<String>(), "twoWord"))
                            ),
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

        assertTrue(
            result is ExpectationResult.Success,
            "Failed to parse: ${(result as? ExpectationResult.DidNotMatch)?.message}"
        )
        assertEquals(
            BranchOneOrTwo.BranchOne("hello"),
            result.stored[rootKey<BranchOneOrTwo>("ambiguous")]
        )
    }

    @Test
    fun either_matches_second_branch() {
        val result = match("(hey 2)")
        assertTrue(
            result is ExpectationResult.Success,
            "Failed to parse: ${(result as? ExpectationResult.DidNotMatch)?.message}"
        )
        assertEquals(
            BranchOneOrTwo.BranchTwo("hey"),
            result.stored[rootKey<BranchOneOrTwo>("ambiguous")]
        )
    }
}
