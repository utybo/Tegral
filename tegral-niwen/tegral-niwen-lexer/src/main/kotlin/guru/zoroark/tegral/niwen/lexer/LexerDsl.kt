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

import guru.zoroark.tegral.niwen.lexer.matchers.*


/**
 * URL to the documentation of Niwen Lexer
 */
internal const val NIWEN_LEXER_DOCS = "tegral.zoroark.guru"

/**
 * Creates a [Lexer] object using the Niwen Lexer DSL, where [body] receives
 * a [LexerBuilder] that can be used to modify the lexer that is
 * eventually returned.
 */
fun niwenLexer(body: LexerBuilder.() -> Unit): Lexer {
    val dslEnv = LexerBuilder()
    body(dslEnv)
    return dslEnv.build().also {
        if (it.statesCount == 0)
            throw NiwenLexerException("Empty body is not allowed. Need help? Visit $NIWEN_LEXER_DOCS")
    }

}

/**
 * Simple function to create a matcher. The returned [TokenMatcher]
 * executes the given lambda and returns its value and nothing else.
 *
 * The behavior of a matcher is described in [TokenMatcher]
 */
fun matcher(
    nextState: NextStateBehavior = NoStateChange,
    matcherBody: (s: String, startAt: Int) -> Token?
): TokenMatcher =
    object : TokenMatcher() {
        override fun match(s: String, startAt: Int): MatcherResult =
            matcherBody(s, startAt)?.let {
                MatchedTokenResult(it, nextState)
            } ?: NoMatchResult
    }