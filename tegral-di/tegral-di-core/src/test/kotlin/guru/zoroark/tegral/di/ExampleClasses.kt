package guru.zoroark.tegral.di

import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.Injector
import guru.zoroark.tegral.di.environment.MetalessInjectionScope
import guru.zoroark.tegral.di.environment.invoke

class ElementClass
class OtherElementClass
class AnotherElementClass

interface ExampleInterface
class ExampleClass : ExampleInterface
class ExampleClass2

object FakeComponent : InjectionScope {
    override fun <T : Any> inject(what: Identifier<T>): Injector<T> {
        error("Cannot inject on fake component")
    }

    override val meta: MetalessInjectionScope
        get() = error("Cannot get meta scope on fake component")

    val fakeProperty: Any? = null
}

class AtoB(scope: InjectionScope) {
    private val b: BtoA by scope()

    val className = "AtoB"

    fun useB() = b.className
}

class BtoA(scope: InjectionScope) {
    private val a: AtoB by scope()

    val className = "BtoA"

    fun useA() = a.className
}

class CtoC(scope: InjectionScope) {
    private val c: CtoC by scope()

    private val className = "CtoC"

    fun useC() = c.className
}
