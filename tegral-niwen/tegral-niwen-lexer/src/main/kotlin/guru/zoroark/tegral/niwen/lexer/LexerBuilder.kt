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

/**
 * This class is used to build a [Lexer] using the Niwen Lexer DSL, and is used
 * in lambda-with-receivers to provide high level DSLs for the configuration of
 * a lexer.
 */
@TegralDsl
class LexerBuilder : Buildable<Lexer>, UnlockDefaultState {
    /**
     * An enum that represents the different kinds of lexers that can be built
     */
    private enum class Kind {
        /**
         * A lexer that uses exactly one default state and zero or more other
         * labeled states
         */
        LABELED_STATES,
        /**
         * A lexer that uses exactly one unlabeled state
         */
        SINGLE_STATE,
        /**
         * Used when the kind of lexer is not known yet
         */
        UNDETERMINED
    }

    /**
     * Indicates whether the kind of lexer that is being built. Refer to [Kind]'s
     * KDoc for more details
     */
    private var lexerKind: Kind = Kind.UNDETERMINED

    /**
     * States being constructed are stored here and are only actually
     * constructed when [build] is called.
     */
    private val constructedStates: MutableMap<StateLabel?, StateBuilder> =
        mutableMapOf()

    /**
     * The state label of the default state. Null by default, can be redefined
     * through the `default state stateLabel` construct.
     */
    private var defaultStateLabel: StateLabel? = null

    /**
     * Create a state using a high-level DSL and add it to this object.
     * The state is not constructed immediately and is only constructed when
     * [build] is called.
     */
    fun state(body: StateBuilder.() -> Unit): Unit =
        when (lexerKind) {
            Kind.LABELED_STATES ->
                throw NiwenLexerException("Cannot create an unlabeled state in a stateful context. You cannot mix labeled states and unlabeled states.")
            Kind.SINGLE_STATE ->
                throw NiwenLexerException("Cannot create multiple unlabeled states. Try adding labels to your states, or using only one state.")
            Kind.UNDETERMINED -> {
                lexerKind = Kind.SINGLE_STATE
                constructedStates[null] = StateBuilder().apply(body)
            }
        }

    @TegralDsl
    infix fun StateLabel.state(body: StateBuilder.() -> Unit): Unit =
        createLabeledState(this, body)

    /**
     * Utility class whose only role is to allow the default state construct.
     * (`default state { ... }` and `default state label`).
     */
    inner class StateInfixCreator internal constructor() {
        /**
         * Create a state.
         */
        @TegralDsl
        infix fun state(body: StateBuilder.() -> Unit) {
            if (defaultStateLabel == null)
                this@LexerBuilder.createLabeledState(null, body)
            else
                throw NiwenLexerException("Default state was already defined as being another state")
        }

        /**
         * Declare that the default state is another labeled state, whose label
         * is provided afterwards.
         */
        infix fun state(labelOfDefaultState: StateLabel) {
            if (lexerKind == Kind.SINGLE_STATE)
                throw NiwenLexerException("Cannot redefine a default state in a single-state lexer.")
            if (lexerKind == Kind.UNDETERMINED)
                lexerKind = Kind.LABELED_STATES
            this@LexerBuilder.defaultStateLabel = labelOfDefaultState
        }
    }

    /**
     * DSL construct that allows you to directly create and go to a default state.
     * Use it like this: `default state { ... }`
     *
     * The only use for this property is for its [state][StateInfixCreator.state] function and the
     * [TokenMatcherBuilder.thenState] function.
     */
    @TegralDsl
    val default = StateInfixCreator()

    /**
     * Utility function that actually performs the addition of labeled states
     */
    private fun createLabeledState(
        label: StateLabel?,
        body: StateBuilder.() -> Unit
    ): Unit = when {
        lexerKind == Kind.SINGLE_STATE ->
            throw NiwenLexerException("Cannot create a labeled state in a single-state context. You cannot mix labeled states and unlabeled states.")
        label == null && constructedStates.containsKey(null) ->
            throw NiwenLexerException("Cannot create two default states. A null label implies that the state is the default state. Use a label for one of the default states or merge both states.")
        constructedStates.containsKey(label) ->
            throw NiwenLexerException("Cannot create two states with the same label. Use a different label so that all states have distinct labels.")
        else -> {
            lexerKind = Kind.LABELED_STATES
            constructedStates[label] = StateBuilder().apply(body)
        }
    }

    override fun build(): Lexer {
        return Lexer(
            states = constructedStates.mapValues { (_, v) -> v.build() },
            defaultStateLabel = defaultStateLabel
        )
    }
}
