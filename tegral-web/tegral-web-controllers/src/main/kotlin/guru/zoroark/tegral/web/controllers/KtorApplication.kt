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

import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.services.api.TegralService
import io.ktor.server.engine.EmbeddedServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val STOP_GRACE_PERIOD_MS = 1000L
private const val STOP_TIMEOUT_MS = 2000L

/**
 * Superclass for Ktor applications that use Tegral Web Application.
 */
abstract class KtorApplication(
    scope: InjectionScope,
    /**
     * The name of this application. Modules are only installed if their [KtorModule.restrictToAppName] matches this
     * value. By default, both `appName` and [KtorModule.restrictToAppName] are set to `null`. `null` is just the
     * default value and does not have any specific meaning (it does NOT mean "put this module everywhere").
     */
    val appName: String? = null
) : TegralService {
    private val ktorExtension: KtorExtension by scope.meta()

    /**
     * Settings used for building the application. These are the same as the ones provided as parameters to
     * Ktor's `embeddedServer` function.
     */
    abstract val settings: KtorApplicationSettings<*, *>

    private var application: EmbeddedServer<*, *>? = null

    override suspend fun start() {
        val server = settings.embeddedServerFromSettings {
            ktorExtension.getModulesForAppName(appName).forEach {
                with(it) { install() }
            }
        }
        application = server.also { it.start() }
    }

    override suspend fun stop(): Unit = withContext(Dispatchers.IO) {
        application?.stop(STOP_GRACE_PERIOD_MS, STOP_TIMEOUT_MS)
    }
}
