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

package guru.zoroark.tegral.di

import guru.zoroark.tegral.core.TegralException
import guru.zoroark.tegral.di.environment.Identifier

/**
 * Type for exceptions directly emitted by Tegral DI.
 *
 * @param message The message for this exception.
 */
@Suppress("UnnecessaryAbstractClass")
abstract class TegralDiException(message: String, cause: Throwable? = null) : TegralException(message, cause)

/**
 * Exception thrown when an operation that requires an extensible injection environment was attempted on a
 * non-extensible environment.
 */
class NotExtensibleException(message: String) : TegralDiException(message)

/**
 * Exception thrown when a component is not found.
 *
 * @property notFound The identifier of the component that was not found
 */
class ComponentNotFoundException(message: String, val notFound: Identifier<*>) : TegralDiException(message) {
    constructor(notFound: Identifier<*>) : this("Component not found: $notFound", notFound)
}

/**
 * Exception thrown when a 'put' or another component declaration is invalid. A declaration can be invalid for any
 * number of reasons.
 */
class InvalidDeclarationException(message: String) : TegralDiException(message)

/**
 * Exception thrown when something went wrong internally in Tegral DI. Unless you are messing around with Tegral DI's
 * internal, you should probably report occurrences of these exceptions (https://github.com/utybo/Tegral/issues),
 * thanks!
 */
class InternalErrorException(message: String, throwable: Throwable? = null) : TegralDiException(message, throwable)

/**
 * Thrown when an extension that needs to be installed was attempted to be used without being installed first.
 */
class ExtensionNotInstalledException(message: String) : TegralDiException(message)

class FailedToResolveException(message: String) : TegralDiException(message)
