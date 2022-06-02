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

package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.TegralDiException

/**
 * Error thrown when a feature that does not exist in controlled test environments (e.g. environments internally used by
 * tegralDiCheck checks) is accessed. If you did not initiate the missing feature yourself:
 *
 * - Ensure that you are using only safe injections (see the `safeInjection` check)
 * - Otherwise, consider reporting it, as it may be a bug from Tegral DI's checks.
 */
class NotAvailableInTestEnvironmentException(message: String) : TegralDiException(message)
