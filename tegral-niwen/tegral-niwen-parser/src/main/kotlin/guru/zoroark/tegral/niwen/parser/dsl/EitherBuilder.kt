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
import guru.zoroark.tegral.niwen.parser.expectations.EitherBranch
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedEither

/**
 * Build an "either" construct, with different branches the parsing process can
 * take.
 *
 * The branches are checked in the order they are declared.
 *
 * Typical use may look like:
 *
 *  ```
 *  MyNode {
 *      expect(...)
 *      either {
 *          // Expectations for branch one...
 *          expect(tokenX1)
 *          expect(OtherNode)
 *      } or {
 *          // Expectations for branch two...
 *          expect(SomethingElse)
 *          expect(tokenY2) storeIn "hello"
 *      } or {
 *          // Expectations for branch three
 *          expect(EvenMoreDifferent)
 *      }
 *      expect(...)
 *  }
 *  ```
 *
 * Anything stored within a branch will be available for the constructed node,
 * provided that the branch was executed.
 *
 * Note: The lambda receives a builder for a single branch, but this function
 * returns the builder for the entire either construct, meaning that:
 *
 * - You cannot add branches from a branch
 *
 * - The recommended way to add branches is to call [or], like in the example
 * above.
 */
@TegralDsl
fun <T> ExpectationReceiver<T>.either(branchBuilder: EitherBranchBuilder<T>.() -> Unit): EitherBuilder<T> {
    val builder = EitherBuilder<T>()
    builder.addBranch(branchBuilder)
    this += builder
    return builder
}

/**
 * The builder for a single branch. This is the receiver type of every [either]
 * branch.
 */
@TegralDsl
class EitherBranchBuilder<T> : ExpectationReceiver<T>, Buildable<EitherBranch<T>> {
    private val expectations = mutableListOf<Buildable<Expectation<T, *>>>()

    override fun plusAssign(expectationBuilder: Buildable<Expectation<T, *>>) {
        expectations += expectationBuilder
    }

    /**
     * Builds this branch
     */
    override fun build(): EitherBranch<T> {
        return EitherBranch(expectations.map { it.build() })
    }
}

/**
 * The builder for the entire either construct. This is different from a single
 * branch.
 */
@TegralDsl
class EitherBuilder<T> : Buildable<ExpectedEither<T>> {
    private val branches = mutableListOf<Buildable<EitherBranch<T>>>()

    /**
     * Build this either expectation
     */
    override fun build(): ExpectedEither<T> {
        return ExpectedEither(branches.map { it.build() })
    }

    /**
     * Add a branch builder to this either builder
     */
    fun addBranch(branch: Buildable<EitherBranch<T>>) {
        branches += branch
    }
}

/**
 * Add a branch to this builder. The branch is first initialized through the
 * [branchInit] argument.
 */
inline fun <T> EitherBuilder<T>.addBranch(branchInit: EitherBranchBuilder<T>.() -> Unit) {
    addBranch(EitherBranchBuilder<T>().apply(branchInit))
}

/**
 * Adds a branch to the `either` construct. `or` can be used multiple times to get more than two branches, like so:
 *
 *  ```
 *  either {
 *      // ...
 *  } or {
 *      // ...
 *  } or {
 *      // ...
 *  }
 *  ```
 */
@TegralDsl
inline infix fun <T> EitherBuilder<T>.or(branchInit: EitherBranchBuilder<T>.() -> Unit): EitherBuilder<T> {
    addBranch(branchInit)
    return this
}
