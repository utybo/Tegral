package org.example.stage1.unit

// Note that the ': TegralTest(...)' is not necessary here, but would be if we
// needed any kind of dependency injection, hence its inclusion
class TestService : TegralTest<Service>(Service::class) {
    @Test
    fun `greet, basic test` = test {
        assertEquals("Hello World!", subject.greet())
    }
}
