package guru.zoroark.tegral.openapi.dsl.repro

import guru.zoroark.tegral.openapi.dsl.openApi
import guru.zoroark.tegral.openapi.dsl.schema
import org.junit.jupiter.api.Test
import kotlin.reflect.typeOf
import kotlin.test.assertFalse
import kotlin.test.assertNull

data class MyClass(val a: String, val b: String)

class Issue40Test {
    @Test
    fun `Issue 40 repro (reified)`() {
        val openApi = openApi {
            "/" get {
                200 response {
                    json { schema<MyClass>() }
                }
            }
        }
        assertNull(openApi.paths["/"]!!.get.responses["200"]!!.content["application/json"]!!.example)
        assertFalse(openApi.paths["/"]!!.get.responses["200"]!!.content["application/json"]!!.exampleSetFlag)
    }

    @Test
    fun `Issue 40 repro (ktype)`() {
        val openApi = openApi {
            "/" get {
                200 response {
                    json { schema(typeOf<MyClass>()) }
                }
            }
        }
        assertNull(openApi.paths["/"]!!.get.responses["200"]!!.content["application/json"]!!.example)
        assertFalse(openApi.paths["/"]!!.get.responses["200"]!!.content["application/json"]!!.exampleSetFlag)
    }
}
