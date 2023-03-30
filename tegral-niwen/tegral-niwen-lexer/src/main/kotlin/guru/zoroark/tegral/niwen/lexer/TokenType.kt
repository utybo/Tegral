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

package guru.zoroark.tegral.niwen.lexer

/**
 * A token type. Can be pretty much anything.
 */
interface TokenType

/**
 * A generic class for token types. [tokenType] returns tokens of this type.
 * Big lexers should be using an enum which implements [TokenType] instead
 * of [tokenType] and [GenericTokenType].
 *
 * @property name A name for this token type, useful for debugging.
 */
class GenericTokenType(val name: String) : TokenType {
    override fun toString(): String = "GenericTokenType[$name]"
}

/**
 * Creates a new, distinct token type and returns it. The returned token type is
 * of the type [GenericTokenType]
 *
 * @param name An optional name for the token type
 */
fun tokenType(name: String = ""): GenericTokenType =
    GenericTokenType(name)
