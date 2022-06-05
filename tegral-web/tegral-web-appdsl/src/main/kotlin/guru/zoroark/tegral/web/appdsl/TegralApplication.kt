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

package guru.zoroark.tegral.web.appdsl

import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import guru.zoroark.tegral.di.services.services
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * This object represents a built and possibly running Tegral application.
 *
 * You can interact with this application by starting or stopping it, inspecting and retrieving elements from its
 * [DI environment][environment], etc.
 */
class TegralApplication(
    /**
     * The dependency injection environment used in this application. All components of this application live here
     * (either directly or via the [meta-environment][ExtensibleInjectionEnvironment.metaEnvironment]).
     */
    val environment: ExtensibleInjectionEnvironment
) {
    /**
     * Starts this application with all its components.
     *
     * Note that you **do not** need to call this method if you are using the [tegral] block, which takes care of
     * launching the application for you.
     */
    suspend fun start() {
        val logger = LoggerFactory.getLogger("tegral.web.appdsl.start")
        environment.services.startAll(logger::info)
    }

    suspend fun stop() {
        val logger = LoggerFactory.getLogger("tegral.web.appdsl.stop")
        environment.services.stopAll(logger::info)
    }
}
