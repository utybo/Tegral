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

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.services.api.TegralService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.test.Test
import kotlin.test.assertEquals

// Tests related to a use-case present here: https://github.com/utybo/Tegral/issues/22
class Issue22Test {
    interface MyServiceContract {
        var startCalls: Int
        var stopCalls: Int
    }

    class MyServiceImpl : MyServiceContract, TegralService {
        private val stateLock = Mutex()
        override var startCalls: Int = 0
        override var stopCalls: Int = 0

        override suspend fun start() {
            stateLock.withLock { startCalls++ }
        }

        override suspend fun stop() {
            stateLock.withLock { stopCalls++ }
        }
    }

    @Test
    fun `Test service start and stop if subclass implements TegralDiService`() {
        val env = tegralDi {
            useServices()
            put<MyServiceContract>(::MyServiceImpl)
        }

        val instance = env.get<MyServiceContract>()
        assertEquals(0, instance.startCalls)
        assertEquals(0, instance.stopCalls)

        runBlocking { env.services.startAll() }
        assertEquals(1, instance.startCalls)
        assertEquals(0, instance.stopCalls)

        runBlocking { env.services.stopAll() }
        assertEquals(1, instance.startCalls)
        assertEquals(1, instance.stopCalls)
    }
}
