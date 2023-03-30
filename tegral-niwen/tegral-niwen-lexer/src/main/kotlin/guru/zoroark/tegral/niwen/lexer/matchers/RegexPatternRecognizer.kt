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

import guru.zoroark.tegral.niwen.lexer.StateBuilder
import org.intellij.lang.annotations.Language
import java.util.regex.Pattern

class RegexPatternRecognizer(private val pattern: Pattern) :
    TokenRecognizer {
    override fun recognize(s: String, startAt: Int): Pair<String, Int>? {
        val matcher = pattern.matcher(s).apply {
            region(startAt, s.length)
            // Look-behind and look-ahead can go beyond region bounds
            useTransparentBounds(true)
            // ^ and $ match the real start and end of the original string
            useAnchoringBounds(false)
        }
        if (!matcher.lookingAt())
            return null
        return matcher.group() to matcher.end()
    }
}

/**
 * Create a recognizer that recognizes the given regular expression. Use
 * this before isToken to create a matcher that matches against a regular
 * expression.
 *
 * @param pattern The regular expression to use in the recognizer
 */
fun StateBuilder.matches(@Language("RegExp") pattern: String): TokenRecognizer {
    return RegexPatternRecognizer(Pattern.compile(pattern))
}
