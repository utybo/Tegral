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

import guru.zoroark.tegral.niwen.lexer.matchers.GoToDefaultState
import guru.zoroark.tegral.niwen.lexer.matchers.GoToLabeledState
import guru.zoroark.tegral.niwen.lexer.matchers.IgnoreMatchResult
import guru.zoroark.tegral.niwen.lexer.matchers.MatchedTokenResult
import guru.zoroark.tegral.niwen.lexer.matchers.NextStateBehavior
import guru.zoroark.tegral.niwen.lexer.matchers.NoMatchResult
import guru.zoroark.tegral.niwen.lexer.matchers.NoStateChange

/**
 * A Lexer is a data class that contains the states that will be used for
 * the lexing process. It is the main entry for using a constructed lexer.
 *
 * The class itself only contains states and not much else.
 *
 * The function [tokenize] can be used to use the lexer on a string
 */
data class Lexer(
    private val states: Map<StateLabel?, LexerState>,
    private val defaultStateLabel: StateLabel? = null
) {
    /**
     * The default state in the case of a multi labeled state lexer, or the
     * only state of the lexer in the case of a single unlabeled state lexer
     */
    val defaultState: LexerState
        get() = states[defaultStateLabel]
            ?: error("No default state in lexer, or the default state label was set to an invalid value")

    /**
     * The amount of states contained within this lexer
     */
    val statesCount: Int
        get() = states.size

    /**
     * The tokenize method will turn a string into a list of tokens based on
     * the [LexerState]s contained in this [Lexer] ([states]) and the
     */
    @Suppress("NestedBlockDepth")
    fun tokenize(s: String): List<Token> {
        var index = 0
        val tokens = mutableListOf<Token>()
        var state = defaultState

        /**
         * A function for updating the lexer's index and state based on what
         * is returned by a matcher
         */
        fun updateParams(newIndex: Int, behavior: NextStateBehavior) {
            index = newIndex
            state = when (behavior) {
                is GoToDefaultState -> defaultState
                is GoToLabeledState -> getState(behavior.stateLabel)
                is NoStateChange -> state
            }
        }
        // While we are in the string
        while (index < s.length) {
            for (matcher in state.matchers) {
                // Attempt to match
                when (val result = matcher.match(s, index)) {
                    is NoMatchResult ->
                        throw NiwenLexerNoMatchException(
                            "No match for string starting at index $index (character: '${s[index]}')"
                        )

                    is IgnoreMatchResult -> with(result) {
                        updateParams(tokenEndsAt, nextStateBehavior)
                    }

                    is MatchedTokenResult -> {
                        val token = result.token
                        checkTokenBounds(token, index, s.length)
                        tokens += token
                        updateParams(token.endsAt, result.nextStateBehavior)
                    }
                }
            }
        }
        return tokens
    }

    private fun checkTokenBounds(
        match: Token,
        index: Int,
        totalLength: Int
    ): Unit = with(match) {
        when {
            string.length > endsAt - startsAt ->
                throw NiwenLexerException(
                    "Returned token string ($string) is too large for the given range ($startsAt-$endsAt)"
                )

            startsAt < index ->
                throw NiwenLexerException(
                    "Incoherent indices: matcher says the token starts at $startsAt when the current index is $index"
                )

            endsAt > totalLength ->
                throw NiwenLexerException(
                    "Incoherent indices: matcher says the token ends at $endsAt, which is out of bounds (total " +
                        "length is $totalLength)"
                )
        }
    }

    /**
     * Get the state with the given label
     *
     * @throws NiwenLexerException if the state was not found
     */
    fun getState(label: StateLabel?): LexerState =
        states[label] ?: throw NiwenLexerException("State with given label not found")
}
