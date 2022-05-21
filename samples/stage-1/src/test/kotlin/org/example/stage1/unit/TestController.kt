package org.example.stage1.unit

class TestController : TegralControllerTest<Controller>(Controller::class) {
    @Test
    fun `Simple controller call`() = test {
        putMock<Service> {
            every { greet() } returns "fromGreet"
        }

        val result = call("/hello")
        assertSuccess(result)
        assertEquals("fromGreet", result.body)
    }
}
