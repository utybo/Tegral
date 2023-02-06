package guru.zoroark.tegral.di.extensions.fundef

import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals

fun Map<String, String>.functionWithManyArgs(
    int1: Int,
    int2: Int,
    anything: Any,
    nullable: String?,
    withGenerics: List<String>,
    optional: String = "Hello"
): Int = TODO()

@OptIn(ExperimentalFundef::class)
class SignatureOfTest {
    fun simpleFunction(): String = TODO()
    fun functionWithArg(myArg: Int): String = TODO()


    @Test
    fun `Simple function`() {
        val actual = signatureOf(::simpleFunction)
        assertEquals(FunctionSignature(mapOf(), typeOf<String>()), actual)
    }

    @Test
    fun `Function with arg`() {
        val actual = signatureOf(::functionWithArg)
        assertEquals(
            FunctionSignature(
                mapOf("myArg" to ParameterSignature(typeOf<Int>())),
                typeOf<String>()
            ),
            actual
        )
    }

    @Test
    fun `Complicated function`() {
        val actual = signatureOf(Map<String, String>::functionWithManyArgs)
        assertEquals(
            FunctionSignature(
                mapOf(
                    "int1" to ParameterSignature(typeOf<Int>()),
                    "int2" to ParameterSignature(typeOf<Int>()),
                    "anything" to ParameterSignature(typeOf<Any>()),
                    "nullable" to ParameterSignature(typeOf<String?>()),
                    "withGenerics" to ParameterSignature(typeOf<List<String>>()),
                    "optional" to ParameterSignature(typeOf<String>(), true)
                ),
                typeOf<Int>(),
                extensionSignature = ParameterSignature(typeOf<Map<String, String>>())
            ),
            actual
        )
    }
}
