package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import kotlin.reflect.typeOf
import kotlin.test.*

class TypeDescriptionTest {
    private val dummyOneKey = NodeParameterKey<Nothing, String>(typeOf<String>(), "One")

    @Test
    fun argument_retrieval_works() {
        val ptd = TypeDescription(mapOf(dummyOneKey to "Two"))
        val str: String = ptd[dummyOneKey]
        assertEquals("Two", str)
    }

    @Test
    fun argument_retrieval_wrong_type_fails() {
        val ptd = TypeDescription(mapOf(dummyOneKey to "Two"))
        assertFailsWith<NiwenParserException> {
            val str: Int = ptd[NodeParameterKey<Nothing, Int>(typeOf<Int>(), "One")]
            println("But $str is not an integer!") // Should not happen
        }.apply {
            val msg = message
            assertNotNull(msg)
        }
    }

    @Test
    fun argument_retrieval_key_absent_fails() {
        val ptd = TypeDescription(mapOf(dummyOneKey to "Two"))
        assertFailsWith<NiwenParserException> {
            val str: String = ptd[NodeParameterKey(typeOf<String>(), "Three")]
            println("But $str does not exist!") // Should not happen
        }.apply {
            val msg = message
            assertNotNull(msg)
            assertTrue(msg.contains("does not exist") && msg.contains("Three"))
        }
    }

}