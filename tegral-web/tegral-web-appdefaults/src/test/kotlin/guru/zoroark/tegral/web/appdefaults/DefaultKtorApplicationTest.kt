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

import guru.zoroark.tegral.config.core.TegralConfig
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.test.TegralSubjectTest
import guru.zoroark.tegral.web.config.WebConfiguration
import io.ktor.server.netty.Netty
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultKtorApplicationTest : TegralSubjectTest<DefaultKtorApplication>(::DefaultKtorApplication) {
    @Test
    fun `Default ktor application uses configuration`() = test {
        val mockSettings = mockk<WebConfiguration> {
            every { port } returns 1010
            every { host } returns "exampleHost"
        }
        put { TegralConfig(mapOf(WebConfiguration to mockSettings)) }

        val actualSettings = subject.settings

        assertEquals(Netty, actualSettings.engine)
        assertEquals(1010, actualSettings.port)
        assertEquals("exampleHost", actualSettings.host)
        assertEquals(listOf(File(".").canonicalPath), actualSettings.watchPaths)

        verify {
            mockSettings.host
            mockSettings.port
        }
    }
}
