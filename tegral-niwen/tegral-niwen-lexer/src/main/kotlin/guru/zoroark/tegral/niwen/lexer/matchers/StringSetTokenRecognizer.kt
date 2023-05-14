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
 * A [StringSetTokenRecognizer] is a [TokenRecognizer] specifically
 * built to be able to recognize whether a substring of the input matches at a
 * relatively high speed. The speed improvements are mostly noticeable when the
 * input consists of strings of characters of the same length.
 *
 * Internally, this recognizer uses a map from integers (length of strings to
 * detect) to a set of strings (the strings of said length). This allows for a
 * fairly efficient recognizer.
 *
 * These recognizers should be created using the `anyOf` DSL function.
 */
class StringSetTokenRecognizer(stringsToRecognize: List<String>) : TokenRecognizer {
    private val criteria: Map<Int, Set<String>>

    init {
        val c = HashMap<Int, HashSet<String>>()
        stringsToRecognize.forEach {
            val x = c.getOrPut(it.length) { HashSet() }
            if (!x.add(it)) throw NiwenLexerException("Duplicate element in multi-string recognizer initialization")
        }
        criteria = c
    }

    override fun recognize(s: String, startAt: Int): Pair<String, Int>? {
        for ((len, possibilities) in criteria) {
            if (startAt + len > s.length) continue
            val sub = s.subSequence(startAt, startAt + len)
            if (possibilities.contains(sub)) return sub.toString() to (startAt + len)
        }
        return null
    }
}

/**
 * Create a recognizer that recognizes any of the strings provided as
 * parameters.
 *
 * @param s Strings that should be recognized
 * @return A string recognizer. Use [isToken] to make it a usable matcher.
 */
fun anyOf(vararg s: String): TokenRecognizer =
    if (s.isEmpty()) {
        throw NiwenLexerException("anyOf() must have at least one string argument")
    } else {
        StringSetTokenRecognizer(s.asList())
    }
