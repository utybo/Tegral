package guru.zoroark.tegral.di.test.mockk

import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.test.TegralSubjectTest
import io.mockk.every
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals

interface ExampleService {
    fun doSomething(): String
}

class ExampleServiceUser(scope: InjectionScope) {
    private val service: ExampleService by scope()

    fun useService(): String = service.doSomething()
}

class TestMockedComponent : TegralSubjectTest<ExampleServiceUser>(::ExampleServiceUser) {
    @Test
    fun `Test with mocked example service`() = test {
        val exampleService = putMock<ExampleService> {
            every { doSomething() } returns "OK"
        }
        assertEquals("OK", subject.useService())
        verify(exactly = 1) { exampleService.doSomething() }
    }
}
