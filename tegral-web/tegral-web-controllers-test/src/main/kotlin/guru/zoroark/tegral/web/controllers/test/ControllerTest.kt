package guru.zoroark.tegral.web.controllers.test

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.test.TegralAbstractSubjectTest
import guru.zoroark.tegral.di.test.TestMutableInjectionEnvironment
import guru.zoroark.tegral.di.test.UnsafeMutableEnvironment
import guru.zoroark.tegral.web.controllers.KtorModule
import guru.zoroark.tegral.web.controllers.filterIsKclassSubclassOf
import guru.zoroark.tegral.web.controllers.getKtorModulesByPriority
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.ClientProvider
import io.ktor.server.testing.TestApplicationBuilder
import io.ktor.server.testing.testApplication
import kotlin.reflect.KClass

abstract class TegralControllerTest<TSubject : Any>(
    subjectClass: KClass<TSubject>,
    private val baseModule: InjectableModule,
    private val appName: String? = null
) : TegralAbstractSubjectTest<TSubject, ControllerTestContext>(subjectClass) {

    @TegralDsl
    override fun <T> test(
        additionalBuilder: ContextBuilderDsl.() -> Unit,
        block: suspend ControllerTestContext.() -> T
    ): T {
        // Very dirty, but testApplication is not inline and does not have a contract :(
        val resultRef = mutableListOf<T>()
        // Initialize base test environment
        val env = tegralDi(UnsafeMutableEnvironment) {
            put(baseModule)
            additionalBuilder()
        }
        // Initialize application client
        val modules =
            env.getKtorModulesByPriority(
                env.components.keys.asSequence().filterIsKclassSubclassOf<KtorModule>(),
                appName
            )
        testApplication {
            application { modules.forEach { with(it) { install() } } }
            env.put { this@testApplication }
            val context = DefaultControllerTestContext(this@testApplication, env)
            resultRef += context.block()
        }
        return resultRef.singleOrNull()
            ?: error("Internal error: resultRef does not contain exactly one result. Please report this.")
    }
}

interface ControllerTestContext : TestMutableInjectionEnvironment, ClientProvider {
    fun applicationBuilder(block: TestApplicationBuilder.() -> Unit)
}

class DefaultControllerTestContext(
    private val appBuilder: ApplicationTestBuilder,
    private val environment: TestMutableInjectionEnvironment
) : ControllerTestContext, TestMutableInjectionEnvironment by environment, ClientProvider by appBuilder {
    override fun applicationBuilder(block: TestApplicationBuilder.() -> Unit) {
        appBuilder.block()
    }
}
