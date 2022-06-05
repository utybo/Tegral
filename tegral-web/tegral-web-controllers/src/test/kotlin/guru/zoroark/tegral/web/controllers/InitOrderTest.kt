package guru.zoroark.tegral.web.controllers


import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.services.services
import guru.zoroark.tegral.di.services.useServices
import io.ktor.server.application.Application
import io.ktor.server.netty.Netty
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class InitOrderTest {
    class StateChecker {
        var state: Int = 0
    }

    class ModuleA(scope: InjectionScope) : KtorModule(10) {
        private val state: StateChecker by scope()

        override fun Application.install() {
            synchronized(state) {
                assertEquals(4, state.state)
                state.state++
            }
        }
    }

    class ModuleB(scope: InjectionScope) : KtorModule() {
        private val state: StateChecker by scope()

        override fun Application.install() {
            synchronized(state) {
                assertEquals(3, state.state)
                state.state++
            }
        }
    }

    class ModuleC(scope: InjectionScope) : KtorModule(1000) {
        private val state: StateChecker by scope()

        override fun Application.install() {
            synchronized(state) {
                assertEquals(2, state.state)
                state.state++
            }
        }
    }

    class KtorApp(scope: InjectionScope) : KtorApplication(scope) {
        private val state: StateChecker by scope()

        override val settings get() = KtorApplicationSettings(Netty, 28830)

        override fun Application.setup() {
            synchronized(state) {
                assertEquals(1, state.state)
                state.state++
            }
        }
    }

    @Test
    fun `Test installation order`() {
        val env = tegralDi {
            useServices()
            meta { put(::KtorExtension) }

            put(::KtorApp)
            put(::ModuleA)
            put(::ModuleB)
            put(::ModuleC)
            put(::StateChecker)
        }
        val state = env.get<StateChecker>()
        assertEquals(0, state.state)
        state.state++
        runBlocking {
            env.services.startAll()
            synchronized(state) {
                assertEquals(5, state.state)
            }
            env.services.stopAll()
        }
    }
}
