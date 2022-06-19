package guru.zoroark.tegral.di.services

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.services.api.TegralService
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

// Test related to a use-case present here: https://github.com/utybo/Tegral/issues/22
class Issue22Test {
    interface MyServiceContract {
        var running: Boolean?
    }

    class MyServiceImpl : MyServiceContract, TegralService {
        override var running: Boolean? = null
        override suspend fun start() {
            running = true
        }

        override suspend fun stop() {
            running = false
        }

    }

    @Test
    fun `Test service starts if subclass implements TegralDiService`() {
        val env = tegralDi {
            useServices()
            put<MyServiceContract>(::MyServiceImpl)
        }

        val instance = env.get<MyServiceContract>()

        runBlocking { env.services.startAll() }
        assertEquals(true, instance.running)

        runBlocking { env.services.stopAll() }
        assertEquals(false, instance.running)
    }
}
