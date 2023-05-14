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
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedOptional

/**
 * Create an optional branch. If the optional branch matches, what it stores
 * will is passed transparently, just like if the `optional { }` was not there.
 * If the optional branch does not match, nothing happens, just like if the
 * entire optional branch was not there.
 *
 * Typical usage may look like:
 *
 *  ```
 *  MyNode {
 *      // Everything here must be present in order to have a match...
 *      expect(...)
 *      expect(...)
 *      optional {
 *          // This branch is optional
 *          expect(...)
 *      }
 *  }
 */
@TegralDsl
fun <T> ExpectationReceiver<T>.optional(optionalBlock: OptionalBranchBuilder<T>.() -> Unit) {
    this += OptionalBranchBuilder<T>().apply(optionalBlock)
}

/**
 * Builder class for an optional expectation.
 */
class OptionalBranchBuilder<T> :
    ExpectationReceiver<T>, Buildable<Expectation<T, *>> {
    private val expectations = mutableListOf<Buildable<Expectation<T, *>>>()

    override fun plusAssign(expectationBuilder: Buildable<Expectation<T, *>>) {
        expectations += expectationBuilder
    }

    /**
     * Build this expected optional
     */
    override fun build(): Expectation<T, *> {
        return ExpectedOptional(expectations.map { it.build() })
    }
}
