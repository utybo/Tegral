package guru.zoroark.tegral.web.controllers.test

import guru.zoroark.tegral.web.controllers.KtorController
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

class ControllerTestTest {
    class Controller : KtorController() {
        override fun Routing.install() {
            get("/test") {
                call.respondText("Hello World!")
            }
        }
    }

    class ControllerTest : TegralControllerTest<Controller>(::Controller) {
        fun testSimpleGet() = test {
            val result = client.get("/test")
            assert2xx(result)
            assertEquals("Hello World!", result.bodyAsText())
        }

        fun testWithCustomization() = test {
            applicationBuilder {
                routing {
                    get("/fromBuilder") {
                        call.respondText("Hello from the builder")
                    }
                }
            }

            val result = client.get("/fromBuilder")
            assert2xx(result)
            assertEquals("Hello from the builder", result.bodyAsText())
        }
    }

    @Test
    fun `test simple controller test`() {
        assertDoesNotThrow { ControllerTest().testSimpleGet() }
    }

    @Test
    fun `can further customize controller test`() {
        assertDoesNotThrow { ControllerTest().testWithCustomization() }
    }
}
