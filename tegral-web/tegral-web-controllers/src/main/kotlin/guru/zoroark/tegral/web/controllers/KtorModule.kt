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

package guru.zoroark.tegral.web.controllers

import io.ktor.server.application.Application

/**
 * A Ktor module that will be automatically installed to a corresponding [KtorApplication] with a matching
 * [app name][restrictToAppName].
 *
 * If you wish to add a controller (a module that only calls `routing` and adds routes), consider using [KtorController]
 * instead.
 */
abstract class KtorModule(
    /**
     * The installation priority of this module within the application. Modules are installed from largest to smallest
     * priority. The default value is available in the [DEFAULT_PRIORITY] constant.
     */
    val moduleInstallationPriority: Int = DEFAULT_MODULE_PRIORITY,
    /**
     * The application name that this module should be installed in. This should correspond to the application name
     * specified in [KtorApplication.appName]
     */
    val restrictToAppName: String? = null
) {
    companion object {
        /**
         * The default priority for modules
         *
         * @see KtorModule.moduleInstallationPriority
         */
        const val DEFAULT_MODULE_PRIORITY = 500

        /**
         * The default priority for controllers
         *
         * @see KtorModule.moduleInstallationPriority
         */
        const val DEFAULT_CONTROLLER_PRIORITY = 300
    }

    /**
     * Install this module on the given application.
     */
    abstract fun Application.install()
}
