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

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.niwen.lexer.matchers.*

abstract class TokenMatcherBuilder(
    /**
     * The original recognizer that should be used by the matcher that will be
     * built
     */
    var baseRecognizer: TokenRecognizer
): Buildable<TokenMatcher> {
    /**
     * Which state the built matcher will lead to, in the form of a
     * [NextStateBehavior] object.
     *
     * @see thenState
     * @see NextStateBehavior
     */
    protected var nextStateBehavior: NextStateBehavior = NoStateChange

    /**
     * Specifies that, once a match is found, the lexer should use the given
     * state [next]. Valid values are:
     *
     * * [default][LexerBuilder.default] or `null` to go to the default
     *   state
     * * A [StateLabel] to go to the state with the given label
     */
    infix fun thenState(next: StateLabel) {
        nextStateBehavior = GoToLabeledState(next)
    }

    infix fun thenState(
        @Suppress("UNUSED_PARAMETER")
        defaultMarker: LexerBuilder.StateInfixCreator
    ) {
        nextStateBehavior = GoToDefaultState
    }
}