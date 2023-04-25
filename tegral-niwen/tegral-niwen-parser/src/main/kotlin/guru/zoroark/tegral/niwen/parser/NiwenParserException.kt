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

package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.core.TegralException

/**
 * Exception type for anything thrown by Niwen Parser.
 *
 * Note that individual expectations should not throw these exceptions when
 * they do not match: instead they should return a
 * [ExpectationResult.DidNotMatch] object. A `NiwenParserException` being thrown
 * indicates that something is very wrong, for example that the parser is
 * not configured properly.
 */
class NiwenParserException(message: String, cause: Throwable? = null) :
    TegralException(message, cause)
