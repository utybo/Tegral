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
