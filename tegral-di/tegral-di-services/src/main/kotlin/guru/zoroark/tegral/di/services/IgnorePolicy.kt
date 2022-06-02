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

package guru.zoroark.tegral.di.services

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.extensions.DeclarationTag

/**
 * Additional policies that can be applied to components to control how the Service extension will treat them.
 *
 * By default, any component whose class implements [TegralService] will be started and stopped when requested.
 * Policies allow you to prevent this.
 */
enum class IgnorePolicy : DeclarationTag {
    /**
     * Components with this policy will not be started nor stopped by the Services extension.
     *
     * Use `put(...) with noService` to apply this policy to a component.
     */
    IgnoreAll,

    /**
     * Components with this policy will not be started by the Services extension, but will be stopped on demand.
     *
     * Use `put(...) with noServiceStart` to apply this policy to a component.
     */
    IgnoreStart,

    /**
     * Components with this policy will not be stopped by the Services extension, but will be started on demand.
     *
     * Use `put(...) with noServiceStop` to apply this policy to a component.
     */
    IgnoreStop
}

internal operator fun IgnorePolicy.plus(other: IgnorePolicy): IgnorePolicy =
    when (this) {
        other -> this // Same level
        else -> IgnorePolicy.IgnoreAll // Different levels (All + Start/Stop) or (Start + Stop) => Always results in All
    }

/**
 * Disables this service: it will not be started or stopped when using [ServiceManager.startAll] or
 * [ServiceManager.stopAll].
 */
@TegralDsl
val noService = IgnorePolicy.IgnoreAll

/**
 * Disables this service: it will not be started when using [ServiceManager.startAll], but will be stopped when using
 * [ServiceManager.stopAll].
 */
@TegralDsl
val noServiceStart = IgnorePolicy.IgnoreStart

/**
 * Disables this service: it will not be started when using [ServiceManager.stopAll], but will be stopped when using
 * [ServiceManager.startAll].
 */
@TegralDsl
val noServiceStop = IgnorePolicy.IgnoreStop
