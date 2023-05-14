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

package guru.zoroark.tegral.prismakt.generator

import guru.zoroark.tegral.prismakt.generator.generators.ScalarType
import guru.zoroark.tegral.prismakt.generator.generators.ScalarTypeWithAccuracy
import guru.zoroark.tegral.prismakt.generator.generators.accurate
import guru.zoroark.tegral.prismakt.generator.generators.inaccurate
import guru.zoroark.tegral.prismakt.generator.generators.nativeTypeToScalarType
import guru.zoroark.tegral.prismakt.generator.parser.NiwenPrism
import guru.zoroark.tegral.prismakt.generator.parser.PField
import guru.zoroark.tegral.prismakt.generator.parser.PModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ScalarTypeTest {
    private val testModel = """
        model AllStrings {
              someString     String
              someText       String @db.Text
              someTinyText   String @db.TinyText
              someMediumText String @db.MediumText
              someLongText   String @db.LongText
              someChars      String @db.Char(20)
              someNChars     String @db.NChar(20)
              someVarChar    String @db.VarChar(20)
              someNVarChar   String @db.NVarChar(20)
              someBit        String @db.Bit(20)
              someVarBit     String @db.VarBit(20)
              someUuid       String @db.Uuid
              someWhat       String @db.What
        }
    """.trimIndent()

    private fun parseAndGetModelAndField(fieldName: String): PField {
        val parsed = NiwenPrism.parse(NiwenPrism.tokenize(testModel)).orThrow()
        val model = parsed.elements.first() as PModel
        return model.fields.first { it.name == fieldName }
    }

    private fun getScalarType(fieldName: String): ScalarTypeWithAccuracy {
        val field = parseAndGetModelAndField(fieldName)
        return assertNotNull(nativeTypeToScalarType(field.name, field.type, field.attributes))
    }

    @Test
    fun stringType() {
        assertEquals(ScalarType.TString.accurate(), getScalarType("someString"))
    }

    @Test
    fun textType() {
        assertEquals(ScalarType.TText.accurate(), getScalarType("someText"))
    }

    @Test
    fun tinyTextType() {
        assertEquals(
            ScalarType.TText.inaccurate(
                "Using Text instead of TinyText because Exposed does not have support for TinyText"
            ),
            getScalarType("someTinyText")
        )
    }

    @Test
    fun mediumTextType() {
        assertEquals(ScalarType.TMediumText.accurate(), getScalarType("someMediumText"))
    }

    @Test
    fun longTextType() {
        assertEquals(ScalarType.TLongText.accurate(), getScalarType("someLongText"))
    }

    @Test
    fun charType() {
        assertEquals(ScalarType.TChar(20).accurate(), getScalarType("someChars"))
    }

    @Test
    fun ncharType() {
        assertEquals(ScalarType.TChar(20).accurate(), getScalarType("someNChars"))
    }

    @Test
    fun varcharType() {
        assertEquals(ScalarType.TVarChar(20).accurate(), getScalarType("someVarChar"))
    }

    @Test
    fun nvarcharType() {
        assertEquals(ScalarType.TVarChar(20).accurate(), getScalarType("someNVarChar"))
    }

    @Test
    fun bitType() {
        assertEquals(
            ScalarType.TString.inaccurate("Bit and VarBit are not supported, using String instead."),
            getScalarType("someBit")
        )
    }

    @Test
    fun varBitType() {
        assertEquals(
            ScalarType.TString.inaccurate("Bit and VarBit are not supported, using String instead."),
            getScalarType("someVarBit")
        )
    }

    @Test
    fun uuidType() {
        assertEquals(ScalarType.TUuid.accurate(), getScalarType("someUuid"))
    }

    @Test
    fun whatType() {
        assertEquals(
            ScalarType.TString.inaccurate("'@db.What' is unknown"),
            getScalarType("someWhat")
        )
    }
}

// TODO more tests there
