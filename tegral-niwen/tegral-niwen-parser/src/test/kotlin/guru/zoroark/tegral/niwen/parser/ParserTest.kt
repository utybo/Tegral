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

import guru.zoroark.tegral.niwen.lexer.matchers.matches
import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedToken
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.StoreStateCallback
import guru.zoroark.tegral.niwen.parser.expectations.asKey
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParserTest {

    @Suppress("UtilityClassWithPublicConstructor")
    class SimpleType {
        companion object : ParserNodeDeclaration<SimpleType> by reflective()
    }

    class StoringType(val child: SimpleType) {
        companion object : ParserNodeDeclaration<StoringType> by reflective()
    }

    @Test
    fun storing_parser_test() {
        val tokenOne = tokenType()
        val parser = NiwenParser(
            types = listOf(
                DescribedType(
                    type = SimpleType,
                    expectations = listOf(ExpectedToken(tokenOne))
                ),
                DescribedType(
                    type = StoringType,
                    expectations = listOf(
                        ExpectedNode(
                            SimpleType,
                            stateCallback = StoreStateCallback(StoringType::child.asKey())
                        )
                    )
                )
            ),
            rootType = StoringType
        )
        val lexer = niwenLexer {
            state {
                "hello" isToken tokenOne
            }
        }
        val tokens = lexer.tokenize("hello")
        val ast = parser.parse(tokens)
        @Suppress("USELESS_IS_CHECK")
        assertTrue(ast is StoringType)
        @Suppress("USELESS_IS_CHECK")
        assertTrue(ast.child is SimpleType)
    }

    @Test
    fun basic_parser_test() {
        val token = tokenType()
        val parser = NiwenParser(
            types = listOf(
                DescribedType(
                    type = SimpleType,
                    expectations = listOf(ExpectedToken(token))
                )
            ),
            rootType = SimpleType
        )
        val lexer = niwenLexer {
            state {
                "hello" isToken token
            }
        }
        val tokens = lexer.tokenize("hello")
        val ast = parser.parse(tokens)
        @Suppress("USELESS_IS_CHECK")
        assertTrue(ast is SimpleType)
    }

    data class NumberNode(val value: String) {
        companion object : ParserNodeDeclaration<NumberNode> by reflective()
    }

    data class AdditionNode(val first: NumberNode, val second: NumberNode) {
        companion object : ParserNodeDeclaration<AdditionNode> by reflective()
    }

    @Test
    fun addition_parser_test() {
        val tokenNumber = tokenType()
        val tokenPlus = tokenType()
        val lexer = niwenLexer {
            state {
                matches("\\d+") isToken tokenNumber
                "+" isToken tokenPlus
                " ".ignore
            }
        }
        val tokens = lexer.tokenize("123 + 4567")
        val parser = NiwenParser(
            listOf(
                DescribedType(
                    AdditionNode,
                    listOf(
                        ExpectedNode(NumberNode, StoreStateCallback(AdditionNode::first.asKey())),
                        ExpectedToken(tokenPlus),
                        ExpectedNode(NumberNode, StoreStateCallback(AdditionNode::second.asKey()))
                    )
                ),
                DescribedType(
                    NumberNode,
                    listOf(
                        ExpectedToken(
                            tokenNumber,
                            stateCallback = StoreStateCallback(NodeParameterKey(typeOf<String>(), "value"))
                        )
                    )
                )
            ),
            AdditionNode
        )
        val ast = parser.parse(tokens)
        assertEquals(AdditionNode(NumberNode("123"), NumberNode("4567")), ast)
    }

    data class LiterallyAString(val value: String) {
        companion object : ParserNodeDeclaration<LiterallyAString> by reflective()
    }
}
