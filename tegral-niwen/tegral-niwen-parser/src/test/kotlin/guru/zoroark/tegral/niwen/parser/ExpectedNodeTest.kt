package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import kotlin.reflect.typeOf
import kotlin.test.*

class ExpectedNodeTest {
    class One {
        companion object : ParserNodeDeclaration<One> by reflective()
    }

    class Two {
        companion object : ParserNodeDeclaration<Two> by reflective()
    }

    @Test
    fun test_undefined_expected_node_fails() {
        val exp = ExpectedNode<Nothing, Two>(Two)
        assertFailsWith<NiwenParserException> {
            exp.matches(
                ParsingContext(
                    listOf(),
                    mapOf(One to DescribedType(One, listOf()))
                ),
                0
            )
        }.apply {
            val msg = message
            assertNotNull(msg)
            assertTrue(
                msg.contains("Two") && msg.contains("declared"),
                "Expected 'Two' and 'declared' in exception message $msg"
            )
        }
    }

    @Test
    fun test_successful() {
        val key = NodeParameterKey<Nothing, Two>(typeOf<Two>(), "yeet")
        val exp = ExpectedNode(Two, key)
        val res = exp.matches(
            ParsingContext(
                listOf(),
                mapOf(Two to DescribedType(Two, listOf()))
            ), 0
        )
        assertTrue(res is ExpectationResult.Success)
        assertTrue(res.stored[key] is Two)
    }
}