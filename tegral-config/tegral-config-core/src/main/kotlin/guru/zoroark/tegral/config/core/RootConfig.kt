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

package guru.zoroark.tegral.config.core

/**
 * Root configuration for Tegral appliactions.
 *
 * Tegral application configurations, even custom ones, msut contain a way to add `[tegral.*]` blocks as most
 * first-party features use this section to configure their own settings. This interface enforces this pattern even if
 * you use custom data classes for your configuration.
 */
interface RootConfig {
    /**
     * The [TegralConfig] instance for this configuration.
     */
    val tegral: TegralConfig
}
