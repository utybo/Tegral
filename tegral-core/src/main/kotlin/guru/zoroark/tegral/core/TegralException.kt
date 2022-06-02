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

package guru.zoroark.tegral.core

/**
 * Root exception class for all exceptions emitted by Tegral.
 *
 * Note that this class should not be instantiated directly and should be subclassed into more detailed and appropriate
 * exceptions.
 */
@Suppress("UnnecessaryAbstractClass") // Abstract because not meant be used as-is
abstract class TegralException(message: String, cause: Throwable? = null) : Exception(message, cause)
