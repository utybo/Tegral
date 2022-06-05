package guru.zoroark.tegral.di.services

import guru.zoroark.tegral.di.ExtensionNotInstalledException
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.environment.named
import guru.zoroark.tegral.di.extensions.with
import guru.zoroark.tegral.services.api.TegralService
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class FullTestServices {

    enum class Status {
        Initialized,
        Started,
        Stopped
    }

    class SuspendingService : TegralService {
        val status: Status
            get() = _started

        private var _started: Status = Status.Initialized

        override suspend fun start() {
            _started = Status.Started
        }

        override suspend fun stop() {
            _started = Status.Stopped
        }
    }

    class DelayStartStopService(private val delayMillis: Long) : TegralService {
        val status: Status
            get() = _started

        private var _started: Status = Status.Initialized

        override suspend fun start() {
            delay(delayMillis)
            _started = Status.Started
        }

        override suspend fun stop() {
            delay(delayMillis)
            _started = Status.Stopped
        }
    }

    class CrashingService : TegralService {
        override suspend fun start() {
            error("I crash when I start")
        }

        override suspend fun stop() {
            error("I crash when I stop")
        }
    }

    class StopperService(scope: InjectionScope) {
        private val serviceManager: ServiceManager by scope.meta()

        fun doStop() {
            runBlocking { serviceManager.stopAll() }
        }
    }

    @Test
    fun `Test simple suspending service start stop`() {
        val env = tegralDi {
            useServices()

            put(::SuspendingService)
        }
        val service = env.get<SuspendingService>()
        assertEquals(Status.Initialized, service.status)
        runBlocking { env.services.startAll() }
        assertEquals(Status.Started, service.status)
        runBlocking { env.services.stopAll() }
        assertEquals(Status.Stopped, service.status)
    }

    @Test
    fun `Starting and stopping done in parallel`() {
        val env = tegralDi {
            useServices()

            repeat(5) {
                put(named("$it")) { DelayStartStopService(1000) }
            }
        }

        val services = List(5) {
            env.get<DelayStartStopService>(named("$it"))
        }
        services.forEach { assertEquals(Status.Initialized, it.status) }

        val startTime = runBlocking {
            measureTimeMillis { env.services.startAll() }
        }
        assertContains(1000L..1500L, startTime, "All services should have been started in ~1sec")
        services.forEach { assertEquals(Status.Started, it.status) }

        val stopTime = runBlocking {
            measureTimeMillis { env.services.stopAll() }
        }
        assertContains(1000L..1500L, stopTime, "All services should have been stopped in ~1sec")
        services.forEach { assertEquals(Status.Stopped, it.status) }
    }

    @Test
    fun `Component start and stop exclusion`() {
        val env = tegralDi {
            useServices()

            put(::SuspendingService)
            put(named("nope"), ::SuspendingService) with noService
        }

        val suspendingService = env.get<SuspendingService>()
        val suspendingRestrictedService = env.get<SuspendingService>(named("nope"))

        assertEquals(Status.Initialized, suspendingService.status)
        assertEquals(Status.Initialized, suspendingRestrictedService.status)

        runBlocking { env.services.startAll() }

        assertEquals(Status.Started, suspendingService.status)
        assertEquals(Status.Initialized, suspendingRestrictedService.status)

        runBlocking { env.services.stopAll() }

        assertEquals(Status.Stopped, suspendingService.status)
        assertEquals(Status.Initialized, suspendingRestrictedService.status)
    }

    @Test
    fun `Component start exclusion`() {
        val env = tegralDi {
            useServices()

            put(::SuspendingService)
            put(named("nope"), ::SuspendingService) with noServiceStart
        }

        val suspendingService = env.get<SuspendingService>()
        val suspendingNoStartService = env.get<SuspendingService>(named("nope"))

        assertEquals(Status.Initialized, suspendingService.status)
        assertEquals(Status.Initialized, suspendingNoStartService.status)

        runBlocking { env.services.startAll() }

        assertEquals(Status.Started, suspendingService.status)
        assertEquals(Status.Initialized, suspendingNoStartService.status)

        runBlocking { env.services.stopAll() }

        assertEquals(Status.Stopped, suspendingService.status)
        assertEquals(Status.Stopped, suspendingNoStartService.status)
    }

    @Test
    fun `Component stop exclusion`() {
        val env = tegralDi {
            useServices()

            put(::SuspendingService)
            put(named("nope"), ::SuspendingService) with noServiceStop
        }

        val suspendingService = env.get<SuspendingService>()
        val suspendingNoStopService = env.get<SuspendingService>(named("nope"))

        assertEquals(Status.Initialized, suspendingService.status)
        assertEquals(Status.Initialized, suspendingNoStopService.status)

        runBlocking { env.services.startAll() }

        assertEquals(Status.Started, suspendingService.status)
        assertEquals(Status.Started, suspendingNoStopService.status)

        runBlocking { env.services.stopAll() }

        assertEquals(Status.Stopped, suspendingService.status)
        assertEquals(Status.Started, suspendingNoStopService.status)
    }

    @Test
    fun `Component starting time statistics are accurate`() {
        val times = (0L..3L).toList().map { it * 1000 }
        val env = tegralDi {
            useServices()

            times.forEach { timeMillis ->
                put(named(timeMillis.toString())) { DelayStartStopService(timeMillis) }
            }
        }
        val startStats = runBlocking { env.services.startAll() }
        assertEquals(times.size, startStats.size)
        times.forEach { time ->
            assertContains(
                (time - 200)..(time + 500),
                startStats[Identifier(DelayStartStopService::class, named(time.toString()))]
            )
        }

        val stopStats = runBlocking { env.services.stopAll() }
        assertEquals(times.size, startStats.size)
        times.forEach { time ->
            assertContains(
                (time - 200)..(time + 500),
                stopStats[Identifier(DelayStartStopService::class, named(time.toString()))]
            )
        }
    }

    @Test
    fun `Component start exception`() {
        val env = tegralDi {
            useServices()

            put(::SuspendingService)
            put(::CrashingService)
        }
        runBlocking {
            val ex = assertFailsWith<TegralServiceException> {
                env.services.startAll()
            }
            assertEquals(
                "Starting service " +
                    "guru.zoroark.shedinja.extensions.services.FullTestServices.CrashingService " +
                    "(<no qualifier>) failed",
                ex.message
            )
        }
    }

    @Test
    fun `Component stop exception`() {
        val env = tegralDi {
            useServices()

            put(::SuspendingService)
            put(::CrashingService)
        }
        runBlocking {
            val ex = assertFailsWith<TegralServiceException> {
                env.services.stopAll()
            }
            assertEquals(
                "Stopping service " +
                    "guru.zoroark.shedinja.extensions.services.FullTestServices.CrashingService " +
                    "(<no qualifier>) failed",
                ex.message
            )
        }
    }

    @Test
    fun `Stop everything from a component`() {
        val env = tegralDi {
            useServices()

            put(::SuspendingService)
            put(::StopperService)
        }
        runBlocking {
            val service = env.get<SuspendingService>()
            assertEquals(Status.Initialized, service.status)
            runBlocking { env.services.startAll() }
            assertEquals(Status.Started, service.status)
            runBlocking { env.get<StopperService>().doStop() }
            assertEquals(Status.Stopped, service.status)
        }
    }

    @Test
    fun `Fail cleanly when attempting to get services when not installed`() {
        val env = tegralDi {
            put { "Enorme ratio" }
        }
        val ex = assertFailsWith<ExtensionNotInstalledException> {
            env.services
        }
        val message = assertNotNull(ex.message)
        assertContains(message, "Services extension is not installed")
    }
}
