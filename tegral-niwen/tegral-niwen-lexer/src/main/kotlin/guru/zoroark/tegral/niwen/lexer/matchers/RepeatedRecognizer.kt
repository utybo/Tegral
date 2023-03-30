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
 * A recognizer that, using another "base" recognizer, will recognize a
 * repetition of the other recognizer.
 *
 * @property baseRecognizer The recognizer which will be repeatedly used.
 * @property min The minimum amount of repetitions that has to be present for a
 * successful recognition.
 * @property max The maximum amount of repetitions that can be present for a
 * successful recognition.
 */
class RepeatedRecognizer(
    val baseRecognizer: TokenRecognizer,
    val min: Int = 1,
    val max: Int? = null
) :
    TokenRecognizer {
    override fun recognize(s: String, startAt: Int): Pair<String, Int>? {
        var repetitions = 0
        var index = startAt
        var totalString = ""
        while (index < s.length) {
            val (ts, end) = baseRecognizer.recognize(s, index) ?: break
            repetitions += 1
            if (max != null && repetitions > max) {
                return null // Exceeded max allowed repetitions
            }
            index = end
            totalString += ts
        }
        return if (repetitions < min) null else totalString to index
    }
}

/**
 * Create a recognizer that recognizes the given recognizer or pseudo-recognizer
 * 1 or more times in a row.
 *
 * This construct supports any (pseudo-)recognizer that is supported by
 * [toRecognizer].
 *
 * @return A [RepeatedRecognizer] using the given
 * recognizer/pseudo-recognizer.
 */
val Any.repeated: RepeatedRecognizer
    get() = RepeatedRecognizer(toRecognizer(this))

/**
 * Create a recognizer that recognizes the given recognizer or pseudo-recognizer
 * [min] to [max] (inclusive) times in a row. By default, the
 * (pseudo)-recognizer is recognized from 1 to an infinite amount of times.
 *
 * Any recognizer or pseudo-recognizer that is supported by [toRecognizer] can
 * be used here.
 *
 * @param min The minimum amount of repetitions required to get a successful
 * match. If the number of repetitions is below the minimum, the recognition
 * fails.
 *
 * @param max The maximum amount of repetitions required to get a successful
 * match, or null if no such limit should exist. If the number of repetitions
 * exceeds the maximum, the recognition fails.
 *
 * @return A [RepeatedRecognizer] that uses the constraints provided in
 * the parameters.
 */
fun Any.repeated(min: Int = 1, max: Int? = null): RepeatedRecognizer =
    RepeatedRecognizer(toRecognizer(this), min, max)