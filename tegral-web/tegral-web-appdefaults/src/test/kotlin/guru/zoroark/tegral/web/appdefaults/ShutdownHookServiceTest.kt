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

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.services.useServices
import guru.zoroark.tegral.di.test.TegralSubjectTest
import guru.zoroark.tegral.di.test.mockk.putMock
import guru.zoroark.tegral.logging.putLoggerFactory
import guru.zoroark.tegral.services.api.TegralService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verifyAll
import kotlin.test.Test

class ShutdownHookServiceTest : TegralSubjectTest<ShutdownHookService>(
    ShutdownHookService::class,
    {
        put(::ShutdownHookService)
        putLoggerFactory()
    }
) {
    @Test
    fun `Adds hook when started`(): Unit = test {
        val runtime = mockk<Runtime> {
            every { addShutdownHook(any()) } just runs
        }
        val rp = putMock<RuntimeProvider> {
            every { this@putMock.runtime } returns runtime
        }

        subject.start()

        verifyAll {
            rp.runtime
            runtime.addShutdownHook(any())
        }
    }

    @Test
    fun `Tests full application when started`(): Unit = test {
        useServices()

        val mockService = putMock<TegralService> {
            coEvery { stop() } just runs
        }

        subject.shutdownHookReceived()

        coVerify { mockService.stop() }
    }
}
