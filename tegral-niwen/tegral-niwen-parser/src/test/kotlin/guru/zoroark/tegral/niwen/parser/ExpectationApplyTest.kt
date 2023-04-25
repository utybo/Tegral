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

import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedToken
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.StoreStateCallback
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpectationApplyTest {
    private fun makeKey(key: String) = NodeParameterKey<Nothing, String>(typeOf<String>(), key)

    @Test
    fun test_successful_with_no_expectations() {
        val ctx = ParsingContext(listOf(), mapOf(), null)
        val result = ctx.applyExpectations(
            0,
            listOf<Expectation<Nothing, *>>()
        )
        assertTrue(result is ExpectationResult.Success)
        assertTrue(result.stored.isEmpty())
        assertEquals(0, result.nextIndex)
    }

    @Test
    fun test_successful_with_some_expectations_and_storage() {
        val one = tokenType()
        val two = tokenType()
        val exp = listOf<Expectation<Nothing, *>>(
            ExpectedToken(one, stateCallback = StoreStateCallback(makeKey("1"))),
            ExpectedToken(one, stateCallback = StoreStateCallback(makeKey("2"))),
            ExpectedToken(two, stateCallback = StoreStateCallback(makeKey("3"))),
            ExpectedToken(one),
            ExpectedToken(two, stateCallback = StoreStateCallback(makeKey("4")))
        )
        val tokens = niwenLexer {
            state {
                'a'..'c' isToken one
                'd'..'f' isToken two
            }
        }.tokenize("abdce")
        val result = ParsingContext(tokens, mapOf()).applyExpectations(0, exp)
        assertTrue(result is ExpectationResult.Success)
        assertEquals(
            mapOf("1" to "a", "2" to "b", "3" to "d", "4" to "e"),
            result.stored.mapKeys { it.key.name }
        )
    }

    @Test
    fun test_unsuccessful_out_of_tokens() {
        val one = tokenType()
        val two = tokenType()
        val exp = listOf<Expectation<Nothing, String>>(
            ExpectedToken(one, stateCallback = StoreStateCallback(makeKey("1"))),
            ExpectedToken(one, stateCallback = StoreStateCallback(makeKey("2"))),
            ExpectedToken(two, stateCallback = StoreStateCallback(makeKey("3"))),
            ExpectedToken(one),
            ExpectedToken(two, stateCallback = StoreStateCallback(makeKey("4")))
        )
        val tokens = niwenLexer {
            state {
                'a'..'c' isToken one
                'd'..'f' isToken two
            }
        }.tokenize("abdc")
        val result = ParsingContext(tokens, mapOf()).applyExpectations(0, exp)
        assertTrue(result is ExpectationResult.DidNotMatch)
        assertEquals(result.atTokenIndex, 4)
    }

    @Test
    fun test_unsuccessful_no_match() {
        val one = tokenType()
        val two = tokenType()
        val exp = listOf<Expectation<Nothing, String>>(
            ExpectedToken(one, stateCallback = StoreStateCallback(makeKey("1"))),
            ExpectedToken(one, stateCallback = StoreStateCallback(makeKey("2"))),
            ExpectedToken(two, stateCallback = StoreStateCallback(makeKey("3"))),
            ExpectedToken(one),
            ExpectedToken(two, stateCallback = StoreStateCallback(makeKey("4")))
        )
        val tokens = niwenLexer {
            state {
                'a'..'c' isToken one
                'd'..'f' isToken two
            }
        }.tokenize("abdfe")
        val result = ParsingContext(tokens, mapOf()).applyExpectations(0, exp)
        assertTrue(result is ExpectationResult.DidNotMatch)
    }
}
