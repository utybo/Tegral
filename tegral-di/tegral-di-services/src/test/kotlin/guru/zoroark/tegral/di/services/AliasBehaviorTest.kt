package guru.zoroark.tegral.di.services

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.named
import guru.zoroark.tegral.di.extensions.putAlias
import guru.zoroark.tegral.services.api.TegralService
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.test.Test
import kotlin.test.assertEquals

class AliasBehaviorTest {
    class CountedStart : TegralService {
        private val mutex = Mutex()
        var startCount = 0
        var stopCount = 0

        override suspend fun start() {
            mutex.withLock { startCount++ }
        }

        override suspend fun stop() {
            mutex.withLock { stopCount++ }
        }

    }

    @Test
    fun `Alias does not call start service twice`() {
        val env = tegralDi {
            useServices()

            put(::CountedStart)
            putAlias<CountedStart, CountedStart>(aliasQualifier = named("hello"))
        }

        val counted = env.get<CountedStart>()
        runBlocking { env.services.startAll() }
        assertEquals(1, counted.startCount)
    }

    @Test
    fun `Alias does not call stop service twice`() {
        val env = tegralDi {
            useServices()

            put(::CountedStart)
            putAlias<CountedStart, CountedStart>(aliasQualifier = named("hello"))
        }

        val counted = env.get<CountedStart>()
        runBlocking { env.services.stopAll() }
        assertEquals(1, counted.stopCount)
    }
}
