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

import guru.zoroark.tegral.niwen.lexer.NiwenLexerException

/**
 * A token recognizer has the ability to detect a pattern within a string (the
 * exact pattern being entirely up to the recognizer) and then returns the
 * matched pattern as well as the ending index (that is, the index of the last
 * matched character + 1, this is considered to be an exclusive index).
 *
 * Recognizers cannot be used as-is within a lexer, as lexers expect
 * [matchers][TokenMatcher]. To use a recognizer within a lexer, use the
 * `isToken` method inside a
 * state block for a DSL approach, or a [TokenRecognizerMatched] object.
 */
interface TokenRecognizer {
    /**
     * Check whether the string [s] starting from index [startAt] matches
     * the pattern this recognizer uses.
     *
     * * If successful, return a pair with the matched pattern and the first
     * index that is outside of the matched pattern.
     * * If unsuccessful, return `null`
     */
    fun recognize(s: String, startAt: Int): Pair<String, Int>?
}


/**
 * Universal function to turn an object into a recognizer. This function
 * returns a [token recognizer][TokenRecognizer].
 *
 * [x] may be:
 *
 * * A [String], in which case the returned recognizer will attempt to match the
 * string exactly. (Pseudo-recognizer)
 *
 * * A [Char], which is identical to a [String] with a single character.
 *
 * * A [CharRange], in which case the recognizer will attempt to match a single
 * character from the given range. (Pseudo-recognizer)
 *
 * * A [recognizer][TokenRecognizer], in which case [x] will be returned
 * directly. This is useful for having one-fits-all methods that can accommodate
 * both directly taking in a recognizer and a pseudo-recognizer. (Recognizer)
 *
 * If [x] does not match any of the previous types, a [NiwenLexerException] is thrown.
 *
 * @param x An object, which can be either a [String], a [CharRange], or a
 * [TokenRecognizer]
 *
 * @return A recognizer that matches the behaviors stated in the description of
 * this function.
 */
fun toRecognizer(x: Any): TokenRecognizer =
    when (x) {
        is TokenRecognizer -> x
        is String -> StringRecognizer(x)
        is Char -> StringRecognizer(x.toString())
        is CharRange -> CharRangeTokenRecognizer(x)
        else -> throw NiwenLexerException(
            "Unable to convert ${x::class.simpleName} to a recognizer."
        )
    }