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

package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.lexer.TokenType
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedEmitConstant
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedToken

/**
 * Adds an expectation to this node descriptor based on a token type.
 *
 * A token of the given token type is expected at this point.
 */
@TegralDsl
fun <T> ExpectationReceiver<T>.expect(tokenType: TokenType, withValue: String? = null) =
    ExpectationStateCallbackBuilder<T, String> {
        ExpectedToken(tokenType, withValue, it)
    }.also {
        this += it
    }

/**
 * Adds an expectation to this node descriptor based on a node
 *
 * A chain of tokens that corresponds to the given node is expected at this
 * point
 */
@TegralDsl
infix fun <T, R> ExpectationReceiver<T>.expect(node: ParserNodeDeclaration<R>) =
    ExpectationStateCallbackBuilder<T, R> {
        ExpectedNode(node, it)
    }.also {
        this += it
    }

/**
 * Adds an expectation that always succeeds and emits the provided value.
 */
@TegralDsl
fun <T, R> ExpectationReceiver<T>.emit(value: R) =
    ExpectationStateCallbackBuilder<T, R> {
        ExpectedEmitConstant(value, it)
    }.also {
        this += it
    }
