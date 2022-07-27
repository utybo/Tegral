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

package guru.zoroark.tegral.web.appdefaults

import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import guru.zoroark.tegral.di.services.services
import guru.zoroark.tegral.logging.LoggerName
import guru.zoroark.tegral.services.api.TegralService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger

// The Runtime object is exposed via DI like this to make testing easier.
internal class RuntimeProvider {
    val runtime: Runtime = Runtime.getRuntime()
}

/**
 * A service that automatically calls `env.services.stopAll()` on the environment when a shutdown hook is received from
 * the JVM.
 */
@LoggerName("tegral.shutdown")
class ShutdownHookService(scope: InjectionScope) : TegralService {
    private val logger: Logger by scope()
    private val env: ExtensibleInjectionEnvironment by scope.meta()
    private val runtimeProvider: RuntimeProvider by scope()
    private val isStoppingMutex = Mutex()
    private var isStopping = false

    private val shutdownHook = Thread {
        shutdownHookReceived()
    }

    override suspend fun start() {
        runtimeProvider.runtime.addShutdownHook(shutdownHook)
        logger.debug("Shutdown hook added.")
    }

    internal fun shutdownHookReceived() {
        logger.info("Shutdown hook received, stopping application...")
        runBlocking {
            isStoppingMutex.withLock { isStopping = true }
            env.services.stopAll { logger.info(it) }
        }
        logger.info("Application has been stopped.")
    }

    override suspend fun stop() {
        isStoppingMutex.withLock {
            if (!isStopping) runtimeProvider.runtime.removeShutdownHook(shutdownHook)
        }
    }
}
