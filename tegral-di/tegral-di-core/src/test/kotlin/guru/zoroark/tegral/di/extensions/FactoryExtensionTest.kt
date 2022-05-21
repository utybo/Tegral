package guru.zoroark.tegral.di.extensions

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.dsl.tegralDiModule
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.extensions.factory.factory
import guru.zoroark.tegral.di.extensions.factory.putFactory
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.test.assertEquals

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoggerName(val name: String)

class FactoryExtensionTest {
    interface WhatIsYourA {
        val name: String
        fun whatIsYourA(): String
    }

    interface WhatIsYourLogger {
        val loggerName: String
        fun whatIsYourLogger(): String
    }

    class A(who: String) {
        val identity = "I am $who's A"
    }

    class Logger(origin: String, name: String) {
        val identity = "I am $origin's Logger and my name is $name"
    }

    @LoggerName("Banana")
    class B(scope: InjectionScope) : WhatIsYourA, WhatIsYourLogger {
        override val name = "B"
        override val loggerName = "log.B"
        private val a: A by scope.factory()
        private val logger: Logger by scope.factory()

        override fun whatIsYourA(): String = "My A is ${a.identity}"
        override fun whatIsYourLogger(): String =
            "My Logger is ${logger.identity}"
    }

    @LoggerName("Coconut")
    class C(scope: InjectionScope) : WhatIsYourA, WhatIsYourLogger {
        override val name = "C"
        override val loggerName = "log.C"
        private val a: A by scope.factory()
        private val logger: Logger by scope.factory()

        override fun whatIsYourA(): String = "My A is ${a.identity}"
        override fun whatIsYourLogger(): String =
            "My Logger is ${logger.identity}"
    }

    class D(scope: InjectionScope) : WhatIsYourA {
        override val name = "D"
        private val a: A by scope.factory()

        override fun whatIsYourA(): String = "My A is ${a.identity}"
    }

    @LoggerName("Markiplier") // Yeah, it's a dead meme, I know.
    class E(scope: InjectionScope) : WhatIsYourLogger {
        override val loggerName = "log.E"
        private val logger: Logger by scope.factory()

        override fun whatIsYourLogger(): String =
            "My Logger is ${logger.identity}"
    }

    @Test
    fun `Test factory system`() {
        var factoryCallCount = 0
        val module = tegralDiModule {
            putFactory { requester ->
                factoryCallCount++
                A((requester as WhatIsYourA).name)
            }
            put(::B)
            put(::C)
            put(::D)
        }
        val env = tegralDi { put(module) }

        assertEquals(0, factoryCallCount)
        mapOf(
            env.get<B>() to "My A is I am B's A",
            env.get<C>() to "My A is I am C's A",
            env.get<D>() to "My A is I am D's A"
        ).forEach { (k, v) ->
            repeat(3) {
                assertEquals(k.whatIsYourA(), v)
            }
        }
        assertEquals(
            3,
            factoryCallCount,
            "Factory function must be called exactly three times"
        )
    }

    private val KClass<*>.loggerName: String
        get() = findAnnotation<LoggerName>()?.name ?: "(no name)"

    @Test
    fun `Test factory with double stuff system`() {
        var aFactoryCallCount = 0
        var loggerFactoryCallCount = 0
        val module = tegralDiModule {
            putFactory { requester ->
                aFactoryCallCount++
                A((requester as WhatIsYourA).name)
            }
            putFactory { requester ->
                loggerFactoryCallCount++
                Logger(
                    (requester as WhatIsYourLogger).loggerName,
                    requester::class.loggerName
                )
            }

            put(::B)
            put(::C)
            put(::D)
            put(::E)
        }
        val env = tegralDi { put(module) }

        assertEquals(0, aFactoryCallCount)
        assertEquals(0, loggerFactoryCallCount)
        mapOf(
            env.get<B>() to "My A is I am B's A",
            env.get<C>() to "My A is I am C's A",
            env.get<D>() to "My A is I am D's A"
        ).forEach { (k, v) ->
            repeat(3) { assertEquals(k.whatIsYourA(), v) }
        }

        mapOf(
            env.get<B>() to "My Logger is I am log.B's Logger and my name is Banana",
            env.get<C>() to "My Logger is I am log.C's Logger and my name is Coconut",
            env.get<E>() to "My Logger is I am log.E's Logger and my name is Markiplier"
        ).forEach { (k, v) ->
            repeat(3) { assertEquals(k.whatIsYourLogger(), v) }
        }

        assertEquals(
            3,
            loggerFactoryCallCount,
            "Incorrect nb of calls to logger factory"
        )
        assertEquals(3, aFactoryCallCount, "Incorrect nb of calls to A factory")
    }
}
