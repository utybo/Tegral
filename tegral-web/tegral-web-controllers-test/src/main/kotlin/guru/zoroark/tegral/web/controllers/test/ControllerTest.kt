package guru.zoroark.tegral.web.controllers.test

import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.test.TegralDiBaseTest
import guru.zoroark.tegral.di.test.UnsafeMutableEnvironment
import kotlin.reflect.KClass

class TegralControllerTest<T : Any>(subjectClass: KClass<T>, module: InjectableModule) :
    TegralDiBaseTest<T>(subjectClass, module) {
    override fun <T> test(additionalBuilder: ContextBuilderDsl.() -> Unit, block: UnsafeMutableEnvironment.() -> T): T {

    }

    fun UnsafeMutableEnvironment.call(url: String) {
        
    }
}
