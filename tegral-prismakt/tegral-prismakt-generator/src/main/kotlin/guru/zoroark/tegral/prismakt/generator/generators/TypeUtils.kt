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

package guru.zoroark.tegral.prismakt.generator.generators

enum class ScalarTypes {
    Int,
    String,
    Boolean,
    BigInt,
    Float,
    Decimal,
    DateTime,
    Bytes
}

fun parseScalarType(typeName: String): ScalarTypes? {
    return when (typeName) {
        "String" -> ScalarTypes.String
        "Int" -> ScalarTypes.Int
        "Boolean" -> ScalarTypes.Boolean
        "BigInt" -> ScalarTypes.BigInt
        "Float" -> ScalarTypes.Float
        "Decimal" -> ScalarTypes.Decimal
        "DateTime" -> ScalarTypes.DateTime
        "Bytes" -> ScalarTypes.Bytes
        else -> null
    }
}