/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
