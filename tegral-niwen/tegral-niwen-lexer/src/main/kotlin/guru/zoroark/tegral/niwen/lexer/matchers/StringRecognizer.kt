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

/**
 * Implementation of a [TokenRecognizer] that attempts to recognize a
 * given string exactly. It will return a pair with the matched string and the
 * ending index (exclusive) if recognized.
 */
class StringRecognizer(
    /**
     * The string that should be recognized by this
     * [StringRecognizer]
     */
    val toRecognize: String
) : TokenRecognizer {
    override fun recognize(s: String, startAt: Int): Pair<String, Int>? {
        if (startAt + toRecognize.length > s.length) {
            return null // Cannot match (match goes beyond s boundaries)
        }
        for (i in toRecognize.indices) {
            if (s[i + startAt] != toRecognize[i])
                return null // Does not match
        }
        // Everything matched, return a token
        return s.substring(
            startAt,
            startAt + toRecognize.length
        ) to startAt + toRecognize.length

    }

}
