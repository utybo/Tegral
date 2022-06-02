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

package guru.zoroark.tegral.logging

import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.factory.putFactory
import guru.zoroark.tegral.featureful.Feature
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A feature that adds logging support to the application via the `by scope.factory()` syntax.
 *
 * I.e., adds a `Logger` factory to the application.
 */
object LoggingFeature : Feature {
    override val id = "tegral-logging"
    override val name = "Tegral Logging"
    override val description = "Provides logging utilities for Tegral applications"

    override fun ExtensibleContextBuilderDsl.install() {
        putFactory<Logger> { requester -> LoggerFactory.getLogger(requester::class.loggerName) }
    }
}
