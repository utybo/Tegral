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

package guru.zoroark.tegral.di.extensions.fundef

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.named
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

var doNotCallNow = true

fun myFunction(): String {
    if (doNotCallNow) fail("Should not be called now!")
    return "Hello world!"
}

class Greeter {
    fun greet(name: String) = "Hello $name!"
}

fun functionWithDependency(greeter: Greeter): String {
    return "Greeting Sam gives: " + greeter.greet("Sam")
}

interface SomeApi {
    fun whatsYourLetter(): String
}

class ImplementationA : SomeApi {
    override fun whatsYourLetter(): String {
        return "A"
    }
}

class ImplementationB : SomeApi {
    override fun whatsYourLetter(): String {
        return "B"
    }
}

fun functionWithApiDependency(api: SomeApi): String {
    return "The letter is ${api.whatsYourLetter()}"
}

fun functionWithDefault(api: SomeApi = ImplementationA()): String {
    return "The letter with default is ${api.whatsYourLetter()}"
}

fun SomeApi.extensionFunction(someText: String): String {
    return "The $someText is ${whatsYourLetter()}"
}

@OptIn(ExperimentalFundef::class)
class FundefTest {
    @Test
    fun `Trivial fundef`() {
        val env = tegralDi {
            putFundef(::myFunction)
        }
        doNotCallNow = false
        val returnValue = env.getFundefOf(::myFunction).invoke()
        assertEquals("Hello world!", returnValue)
    }

    @Test
    fun `Fundef with dependency`() {
        val env = tegralDi {
            putFundef(::functionWithDependency)
            put(::Greeter)
        }
        val functionWrapper = env.getFundefOf(::functionWithDependency)
        val result = functionWrapper.invoke()
        assertEquals("Greeting Sam gives: Hello Sam!", result)
    }

    @Test
    fun `Fundef with dependency provided at invoke time`() {
        val env = tegralDi {
            putFundef(::functionWithDependency)
        }
        val functionWrapper = env.getFundefOf(::functionWithDependency)
        val result = functionWrapper.invoke(mapOf("greeter" to Greeter()))
        assertEquals("Greeting Sam gives: Hello Sam!", result)
    }

    @Test
    fun `Fundef with dependency that has qualifier`() {
        val env = tegralDi {
            putFundef(
                ::functionWithDependency.configureFundef {
                    "greeter" qualifyWith named("greeter")
                }
            )
            put(named("greeter"), ::Greeter)
        }

        val functionWrapper = env.getFundefOf(::functionWithDependency)
        val result = functionWrapper.invoke()
        assertEquals("Greeting Sam gives: Hello Sam!", result)
    }

    @Test
    fun `Fundef with dependency overridden`() {
        val env = tegralDi {
            put<SomeApi>(::ImplementationA)
            putFundef(::functionWithApiDependency)
        }
        val functionWrapper = env.getFundefOf(::functionWithApiDependency)
        val result = functionWrapper.invoke(mapOf("api" to ImplementationB()))
        assertEquals("The letter is B", result)
    }

    @Test
    fun `Fundef with default`() {
        val env = tegralDi {
            putFundef(::functionWithDefault)
        }
        val functionWrapper = env.getFundefOf(::functionWithDefault)
        val result = functionWrapper.invoke()
        assertEquals("The letter with default is A", result)
    }

    @Test
    fun `Fundef with overridden default`() {
        val env = tegralDi {
            putFundef(::functionWithDefault)
        }
        val functionWrapper = env.getFundefOf(::functionWithDefault)
        val result = functionWrapper.invoke(mapOf("api" to ImplementationB()))
        assertEquals("The letter with default is B", result)
    }

    @Test
    fun `Fundef with extension function`() {
        val env = tegralDi {
            put { "LEttER" }
            putFundef(SomeApi::extensionFunction)
        }
        val functionWrapper = env.getFundefOf(SomeApi::extensionFunction)
        functionWrapper.checkCallable(extension = ImplementationA())
        val result = functionWrapper.invoke(extension = ImplementationA())
        assertEquals("The LEttER is A", result)
    }

    @Test
    fun `Fundef with extension function, overridden regular parameter`() {
        val env = tegralDi {
            put { "NOPE" }
            putFundef(SomeApi::extensionFunction)
        }
        val functionWrapper = env.getFundefOf(SomeApi::extensionFunction)
        functionWrapper.checkCallable(mapOf("someText" to "bruh"), extension = ImplementationB())
        val result = functionWrapper.invoke(mapOf("someText" to "bruh"), extension = ImplementationB())
        assertEquals("The bruh is B", result)
    }
}
