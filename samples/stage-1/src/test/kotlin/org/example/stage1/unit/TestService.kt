package org.example.stage1.unit

import guru.zoroark.tegral.di.test.TegralDiBaseTest
import org.example.stage1.Service
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

// Note that the ': TegralTest(...)' is not necessary here, but would be if we
// needed any kind of dependency injection, hence its inclusion
class TestService : TegralDiBaseTest<Service>(::Service) {
    @Test
    fun `greet, basic test`() = test {
        assertEquals("Hello World!", subject.greet())
    }
}
