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
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.lexer.matchers.TokenMatcher
import guru.zoroark.tegral.niwen.lexer.matchers.toRecognizer

/**
 * This classed is used to build a lexer state ([LexerState]) using a DSL, and is
 * used inside a lambda-with receiver. It also defines high level functions for
 * constructing matchers, such as [anyOf] or [matches], and using custom
 * matchers with the [+ operator][unaryPlus].
 *
 * States themselves are only constructed when calling [build], since
 * [LexerState] objects are immutable and the goal of the DSL is to provide a way
 * to configure them.
 */
@TegralDsl
class StateBuilder : Buildable<LexerState> {
    private val tokenMatchers = mutableListOf<Buildable<TokenMatcher>>()

    override fun build(): LexerState {
        return LexerState(
            mutableListOf<TokenMatcher>().apply {
                addAll(tokenMatchers.map { it.build() })
            }
        )
    }

    /**
     * Add a matcher to this state that matches the recognizer (or
     * pseudo-recognizer) on the left to the token type on the right.
     *
     * The recognizer (the object on the left on which you are calling
     * `isToken`) may be any of the types supported by [toRecognizer].
     *
     * @param token The token to associate `this` to
     * @return A [matcher environment][MatchedMatcherBuilder]
     * that uses the given (pseudo-)recognizers as its recognition technique.
     * @see toRecognizer
     */
    @TegralDsl
    infix fun Any.isToken(token: TokenType): MatchedMatcherBuilder {
        val recognizer = toRecognizer(this)
        val env = MatchedMatcherBuilder(recognizer, token)
        tokenMatchers += env
        return env
    }

    /**
     * Add an already defined matcher to this state
     */
    operator fun TokenMatcher.unaryPlus() {
        tokenMatchers += Buildable.of(this)
    }

    /**
     * Anything that matches the given recognizer (or pseudo-recognizer) exactly
     * will be ignored when encountered. This would be equivalent to a `isToken`
     * that does not actually create any token.
     *
     * The matched sequence is skipped entirely by the lexer. No output is
     * emitted whatsoever.
     *
     * The given recognizer or pseudo-recognizer can be anything that
     * [toRecognizer] supports.
     *
     * @return A matcher environment that will produce a matcher that will make
     * the lexer ignore anything that it matches.
     */
    @TegralDsl
    val Any.ignore: IgnoringMatcherBuilder
        get() {
            val env = IgnoringMatcherBuilder(toRecognizer(this))
            tokenMatchers += env
            return env
        }

    /**
     * Special object for using the default class in the Niwen Lexer DSL
     */
    object Default

    /**
     * State label for the default state.
     *
     * Because the default state is handled differently internally, this is not actually an instance of [StateLabel].
     * All DSL functions that accept a state label can accept either a real [StateLabel] or the [default] value.
     *
     */
    @TegralDsl
    val default = Default
}
