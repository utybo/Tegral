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
import guru.zoroark.tegral.di.environment.named
import guru.zoroark.tegral.di.extensions.putAlias
import guru.zoroark.tegral.services.api.TegralService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class AliasBehaviorTest {
    class CountedStart : TegralService {
        private val mutex = Mutex()
        var startCount = 0
        var stopCount = 0

        override suspend fun start() {
            mutex.withLock { startCount++ }
        }

        override suspend fun stop() {
            mutex.withLock { stopCount++ }
        }
    }

    @Test
    fun `Can get alias with same type but different qualifier`() {
        val env = tegralDi {
            put(::Any)
            putAlias<Any, Any>(aliasQualifier = named("alias"))
        }
        val actual = env.get<Any>()
        val alias = env.get<Any>(named("alias"))
        assertSame(actual, alias)
    }

    interface AContract
    class AImplementation : AContract

    @Test
    fun `Can get alias with different types`() {
        val env = tegralDi {
            put(::AImplementation)
            putAlias<AContract, AImplementation>()
        }
        val actual = env.get<AImplementation>()
        val alias = env.get<AContract>()
        assertSame(actual, alias)
    }

    @Test
    fun `Alias does not call start service twice`() {
        val env = tegralDi {
            useServices()

            put(::CountedStart)
            putAlias<CountedStart, CountedStart>(aliasQualifier = named("hello"))
        }

        val counted = env.get<CountedStart>()
        runBlocking { env.services.startAll() }
        assertEquals(1, counted.startCount)
    }

    @Test
    fun `Alias does not call stop service twice`() {
        val env = tegralDi {
            useServices()

            put(::CountedStart)
            putAlias<CountedStart, CountedStart>(aliasQualifier = named("hello"))
        }

        val counted = env.get<CountedStart>()
        runBlocking { env.services.stopAll() }
        assertEquals(1, counted.stopCount)
    }
}
