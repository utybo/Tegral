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

package guru.zoroark.tegral.niwen.lexer.matchers

import guru.zoroark.tegral.niwen.lexer.StateLabel

/**
 * Subclasses of this class represent a kind of behavior that can be followed
 * when deciding whether to change state or not, usually after a successful
 * match.
 */
sealed class NextStateBehavior

/**
 * Indicates that the state should not be changed
 */
object NoStateChange : NextStateBehavior()

/**
 * Indicates that the state should be changed to the default state
 */
object GoToDefaultState : NextStateBehavior()

/**
 * Indicates that the state should be changed to the state with [stateLabel] as
 * its label.
 *
 * @property stateLabel The label of the state that should be the next state
 * when following this behavior.
 */
class GoToLabeledState(val stateLabel: StateLabel) :
    NextStateBehavior()
