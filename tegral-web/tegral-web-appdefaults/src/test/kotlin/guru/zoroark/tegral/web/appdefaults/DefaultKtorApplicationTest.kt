package guru.zoroark.tegral.web.appdefaults

import guru.zoroark.tegral.config.core.TegralConfig
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.test.TegralSubjectTest
import guru.zoroark.tegral.web.config.WebConfiguration
import guru.zoroark.tegral.web.controllers.KtorApplicationSettings
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
