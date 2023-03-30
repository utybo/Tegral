package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.niwenLexer
import guru.zoroark.tegral.niwen.lexer.tokenType
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedToken
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.applyExpectations
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpectationApplyTest {
    private fun makeKey(key: String) = NodeParameterKey<Nothing, String>(typeOf<String>(), key)

    @Test
    fun test_successful_with_no_expectations() {
        val result = listOf<Expectation<Nothing, *>>().applyExpectations(
            ParsingContext(listOf(), mapOf())
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
            ExpectedToken(one, storeValueIn = makeKey("1")),
            ExpectedToken(one, storeValueIn = makeKey("2")),
            ExpectedToken(two, storeValueIn = makeKey("3")),
            ExpectedToken(one),
            ExpectedToken(two, storeValueIn = makeKey("4"))
        )
        val tokens = niwenLexer {
            state {
                'a'..'c' isToken one
                'd'..'f' isToken two
            }
        }.tokenize("abdce")
        val result = exp.applyExpectations(ParsingContext(tokens, mapOf()), 0)
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
            ExpectedToken(one, storeValueIn = makeKey("1")),
            ExpectedToken(one, storeValueIn = makeKey("2")),
            ExpectedToken(two, storeValueIn = makeKey("3")),
            ExpectedToken(one),
            ExpectedToken(two, storeValueIn = makeKey("4"))
        )
        val tokens = niwenLexer {
            state {
                'a'..'c' isToken one
                'd'..'f' isToken two
            }
        }.tokenize("abdc")
        val result = exp.applyExpectations(ParsingContext(tokens, mapOf()))
        assertTrue(result is ExpectationResult.DidNotMatch)
        assertEquals(result.atTokenIndex, 4)
    }

    @Test
    fun test_unsuccessful_no_match() {
        val one = tokenType()
        val two = tokenType()
        val exp = listOf<Expectation<Nothing, String>>(
            ExpectedToken(one, storeValueIn = makeKey("1")),
            ExpectedToken(one, storeValueIn = makeKey("2")),
            ExpectedToken(two, storeValueIn = makeKey("3")),
            ExpectedToken(one),
            ExpectedToken(two, storeValueIn = makeKey("4"))
        )
        val tokens = niwenLexer {
            state {
                'a'..'c' isToken one
                'd'..'f' isToken two
            }
        }.tokenize("abdfe")
        val result = exp.applyExpectations(ParsingContext(tokens, mapOf()), 0)
        assertTrue(result is ExpectationResult.DidNotMatch)
    }
}