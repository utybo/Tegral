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

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedLookahead

/**
 * A DSL builder for [ExpectedLookahead]. Used by [lookahead].
 */
class LookaheadBuilder<T> : ExpectationReceiver<Nothing>, Buildable<ExpectedLookahead<T>> {
    private val expectationBuilders = mutableListOf<Buildable<Expectation<Nothing, *>>>()
    override fun build(): ExpectedLookahead<T> {
        return ExpectedLookahead(expectationBuilders.map { it.build() })
    }

    override fun plusAssign(expectationBuilder: Buildable<Expectation<Nothing, *>>) {
        expectationBuilders += expectationBuilder
    }
}

/**
 * Create a *lookahead* expectation.
 *
 * Lookahead expectations check that, starting from the current position, everything that follows respects the
 * expectations provided in the [builder] block.
 */
@TegralDsl
fun <T> ExpectationReceiver<T>.lookahead(builder: ExpectationReceiver<Nothing>.() -> Unit) {
    this += LookaheadBuilder<T>().apply(builder)
}
