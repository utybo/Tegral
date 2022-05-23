package guru.zoroark.tegral.di.test

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.getOrNull
import guru.zoroark.tegral.di.environment.invoke
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

interface ServiceContract {
    fun someThing(): String
}

class Controller(scope: InjectionScope) {
    private val service: ServiceContract by scope()

    fun htmlThing() = "<html>${service.someThing()}</html>"
}

class TegralSubjectTestTest {
    @Test
    fun `Test with function constructor`() {
        var wasHit = false

        class ServiceImpl : ServiceContract {
            override fun someThing() = "Hello there!"
        }

        class Test : TegralSubjectTest<Controller>(::Controller) {
            fun myTest() = test {
                put<ServiceContract>(::ServiceImpl)

                assertEquals("<html>Hello there!</html>", subject.htmlThing())
                wasHit = true
            }
        }

        Test().myTest()
        assertTrue(wasHit)
    }

    @Test
    fun `Test getting unknown component`() {
        var wasHit = false
        class SubjectUnderTest
        class Test : TegralSubjectTest<SubjectUnderTest>(SubjectUnderTest::class, { put(::SubjectUnderTest) }) {
            fun myTest() = test {
                assertNull(getOrNull<String>())
                wasHit = true
            }
        }

        Test().myTest()
        assertTrue(wasHit)
    }

    @Test
    fun `Test with module constructor`() {
        var wasHit = false

        class ServiceImpl : ServiceContract {
            override fun someThing() = "Hello there!"
        }

        class Test : TegralSubjectTest<Controller>(Controller::class, { put(::Controller) }) {
            fun myTest() = test {
                put<ServiceContract>(::ServiceImpl)

                assertEquals("<html>Hello there!</html>", subject.htmlThing())
                wasHit = true
            }
        }

        Test().myTest()
        assertTrue(wasHit)
    }

    @Test
    fun `Test with pre-built module constructor`() {
        var wasHit = false

        class ServiceImpl : ServiceContract {
            override fun someThing() = "Hello there!"
        }

        val module = tegralDiModule {
            put { Controller(scope) }
        }

        class Test : TegralSubjectTest<Controller>(Controller::class, module) {
            fun myTest() = test {
                put<ServiceContract>(::ServiceImpl)

                assertEquals("<html>Hello there!</html>", subject.htmlThing())
                wasHit = true
            }
        }

        Test().myTest()
        assertTrue(wasHit)
    }

    interface Repository {
        fun storeThis(text: String)
    }

    @Test
    fun `Test example with MockK`() {
        // Interface defined just above this function

        class Service(scope: InjectionScope) {
            private val repository: Repository by scope()

            fun incomingText(text: String) {
                // ...
                repository.storeThis(text)
                // ...
            }
        }

        // Test code
        class TestService : TegralSubjectTest<Service>(::Service) {
            fun `Accepts incoming text properly`() = test {
                put<Repository> {
                    mockk { every { storeThis("hello") } just runs }
                }

                subject.incomingText("hello")

                verify { get<Repository>().storeThis("hello") }
            }
        }

        TestService().`Accepts incoming text properly`()
    }

    @Test
    fun `Test example with MockK with alsoPut`() {
        // Interface defined just above this function

        class Service(scope: InjectionScope) {
            private val repository: Repository by scope()

            fun incomingText(text: String) {
                // ...
                repository.storeThis(text)
                // ...
            }
        }

        // Test code
        class TestService : TegralSubjectTest<Service>(::Service) {
            fun `Accepts incoming text properly`() = test {
                mockk<Repository> { every { storeThis("hello") } just runs }.also { put { it } }

                subject.incomingText("hello")

                verify { get<Repository>().storeThis("hello") }
            }
        }

        TestService().`Accepts incoming text properly`()
    }
}
