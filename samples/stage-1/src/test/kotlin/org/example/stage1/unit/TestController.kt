package org.example.stage1.unit

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.test.mockk.putMock
import guru.zoroark.tegral.web.controllers.test.TegralControllerTest
import guru.zoroark.tegral.web.controllers.test.assert2xx
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.mockk.every
import org.example.stage1.Controller
import org.example.stage1.Service
import kotlin.test.Test
import kotlin.test.assertEquals

class TestController : TegralControllerTest<Controller>(
        Controller::class,
        tegralDiModule { put(::Controller) }
) {
    @Test
    fun `Simple controller call`() = test {
        putMock<Service> {
            every { greet() } returns "fromGreet"
        }

        val result = client.get("/hello")
        assert2xx(result)
        assertEquals("fromGreet", result.bodyAsText())
    }
}
