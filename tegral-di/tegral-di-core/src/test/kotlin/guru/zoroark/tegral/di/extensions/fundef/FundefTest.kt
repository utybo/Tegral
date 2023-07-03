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
import guru.zoroark.tegral.di.environment.named
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

var doNotCallNow = true

@OptIn(ExperimentalFundef::class)
@Fundef
fun myFunction(): String {
    if (doNotCallNow) fail("Should not be called now!")
    return "Hello world!"
}

class Greeter {
    @OptIn(ExperimentalFundef::class)
    @Fundef
    fun greet(name: String) = "Hello $name!"
}

@OptIn(ExperimentalFundef::class)
@Fundef
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

@OptIn(ExperimentalFundef::class)
@Fundef
fun functionWithApiDependency(api: SomeApi): String {
    return "The letter is ${api.whatsYourLetter()}"
}

@OptIn(ExperimentalFundef::class)
@Fundef
fun functionWithNullableApiDependency(api: SomeApi?): String {
    return if (api == null) "It was null!!!" else "The letter is ${api.whatsYourLetter()}"
}

@OptIn(ExperimentalFundef::class)
@Fundef
fun functionWithDefault(api: SomeApi = ImplementationA()): String {
    return "The letter with default is ${api.whatsYourLetter()}"
}

@OptIn(ExperimentalFundef::class)
@Fundef
fun SomeApi.extensionFunction(someText: String): String {
    return "The $someText is ${whatsYourLetter()}"
}

fun <T> genericDumb(hello: T): String {
    return "hello is $hello"
}

class SomeDumbClass(val dumbProp: String) {
    @OptIn(ExperimentalFundef::class)
    @Fundef
    fun saySomething() = "Something $dumbProp"
}

@OptIn(ExperimentalFundef::class)
class FundefTest {
    @Test
    fun `Trivial fundef (putFundef)`() {
        val env = tegralDi {
            putFundef(::myFunction)
        }
        doNotCallNow = false
        val returnValue = env.getFundefOf(::myFunction).invoke()
        assertEquals("Hello world!", returnValue)
    }

    @Test
    fun `Trivial fundef (put)`() {
        val env = tegralDi {
            put(::myFunction)
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
    fun `Fundef with dependency (put)`() {
        val env = tegralDi {
            put(::functionWithDependency)
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
    fun `Fundef with dependency provided at invoke time (put)`() {
        val env = tegralDi {
            put(::functionWithDependency)
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
    fun `Fundef with dependency overridden (put)`() {
        val env = tegralDi {
            put<SomeApi>(::ImplementationA)
            put(::functionWithApiDependency)
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
    fun `Fundef with default (put)`() {
        val env = tegralDi {
            put(::functionWithDefault)
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
    fun `Fundef with overridden default (put)`() {
        val env = tegralDi {
            put(::functionWithDefault)
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
    fun `Fundef with extension function (put)`() {
        val env = tegralDi {
            put { "LEttER" }
            put(SomeApi::extensionFunction)
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

    @Test
    fun `Fundef with extension function, overridden regular parameter (put)`() {
        val env = tegralDi {
            put { "NOPE" }
            put(SomeApi::extensionFunction)
        }
        val functionWrapper = env.getFundefOf(SomeApi::extensionFunction)
        functionWrapper.checkCallable(mapOf("someText" to "bruh"), extension = ImplementationB())
        val result = functionWrapper.invoke(mapOf("someText" to "bruh"), extension = ImplementationB())
        assertEquals("The bruh is B", result)
    }

    @Test
    fun `Check callable fails if extension is not provided`() {
        val env = tegralDi {
            put<SomeApi>(::ImplementationA)
            putFundef(SomeApi::extensionFunction)
            put { "Test" }
        }
        val functionWrapper = env.getFundefOf(SomeApi::extensionFunction)
        val exc = assertFailsWith<FundefException> { functionWrapper.checkCallable() }
        assertNotNull(exc.message)
        assertTrue { exc.message!!.contains("special parameter") }
    }

    @Test
    fun `Check callable fails if extension is not provided (put)`() {
        val env = tegralDi {
            put<SomeApi>(::ImplementationA)
            put(SomeApi::extensionFunction)
            put { "Test" }
        }
        val functionWrapper = env.getFundefOf(SomeApi::extensionFunction)
        val exc = assertFailsWith<FundefException> { functionWrapper.checkCallable() }
        assertNotNull(exc.message)
        assertTrue { exc.message!!.contains("special parameter") }
    }

    @Test
    fun `Check callable fails if instance is not provided`() {
        val env = tegralDi {
            put(::Greeter)
            putFundef(Greeter::greet)
        }
        val functionWrapper = env.getFundefOf(Greeter::greet)
        val exc = assertFailsWith<FundefException> { functionWrapper.checkCallable() }
        assertNotNull(exc.message)
        assertTrue { exc.message!!.contains("special parameter") }
    }

    @Test
    fun `Check callable fails if instance is not provided (put)`() {
        val env = tegralDi {
            put(::Greeter)
            put(Greeter::greet)
        }
        val functionWrapper = env.getFundefOf(Greeter::greet)
        val exc = assertFailsWith<FundefException> { functionWrapper.checkCallable() }
        assertNotNull(exc.message)
        assertTrue { exc.message!!.contains("special parameter") }
    }

    @Test
    fun `Check callable fails if cannot find injection`() {
        val env = tegralDi {
            putFundef(::functionWithDependency)
        }
        val functionWrapper = env.getFundefOf(::functionWithDependency)
        val exc = assertFailsWith<FundefException> { functionWrapper.checkCallable() }
        assertNotNull(exc.message)
        assertTrue { exc.message!!.contains("not found") }
    }

    @Test
    fun `Check callable fails if cannot find injection (put)`() {
        val env = tegralDi {
            put(::functionWithDependency)
        }
        val functionWrapper = env.getFundefOf(::functionWithDependency)
        val exc = assertFailsWith<FundefException> { functionWrapper.checkCallable() }
        assertNotNull(exc.message)
        assertTrue { exc.message!!.contains("not found") }
    }

    @Test
    fun `Check callable fails if incompatible types`() {
        val env = tegralDi {
            putFundef(::functionWithApiDependency)
        }
        val functionWrapper = env.getFundefOf(::functionWithApiDependency)
        val exc = assertFailsWith<FundefException> { functionWrapper.checkCallable(mapOf("api" to Greeter())) }
        assertNotNull(exc.message)
        assertTrue("Actual message: '${exc.message}'") {
            exc.message!!.contains("is not compatible")
        }
    }

    @Test
    fun `Check callable fails if incompatible types (put)`() {
        val env = tegralDi {
            put(::functionWithApiDependency)
        }
        val functionWrapper = env.getFundefOf(::functionWithApiDependency)
        val exc = assertFailsWith<FundefException> { functionWrapper.checkCallable(mapOf("api" to Greeter())) }
        assertNotNull(exc.message)
        assertTrue("Actual message: '${exc.message}'") {
            exc.message!!.contains("is not compatible")
        }
    }

    @Test
    fun `Check callable fails null check`() {
        val env = tegralDi {
            putFundef(::functionWithApiDependency)
        }
        val functionWrapper = env.getFundefOf(::functionWithApiDependency)
        val exc = assertFailsWith<FundefException> { functionWrapper.checkCallable(mapOf("api" to null)) }
        assertNotNull(exc.message)
        assertTrue("Actual message: '${exc.message}'") {
            exc.message!!.contains("is not compatible")
        }
    }

    @Test
    fun `Check callable fails null check (put)`() {
        val env = tegralDi {
            put(::functionWithApiDependency)
        }
        val functionWrapper = env.getFundefOf(::functionWithApiDependency)
        val exc = assertFailsWith<FundefException> { functionWrapper.checkCallable(mapOf("api" to null)) }
        assertNotNull(exc.message)
        assertTrue("Actual message: '${exc.message}'") {
            exc.message!!.contains("is not compatible")
        }
    }

    @Test
    fun `Check callable successful null check`() {
        val env = tegralDi {
            putFundef(::functionWithNullableApiDependency)
        }
        val functionWrapper = env.getFundefOf(::functionWithNullableApiDependency)
        assertDoesNotThrow { functionWrapper.checkCallable(mapOf("api" to null)) }
    }

    @Test
    fun `Check callable successful null check (put)`() {
        val env = tegralDi {
            put(::functionWithNullableApiDependency)
        }
        val functionWrapper = env.getFundefOf(::functionWithNullableApiDependency)
        assertDoesNotThrow { functionWrapper.checkCallable(mapOf("api" to null)) }
    }

    @Test
    fun `Check callable successful with optionality`() {
        val env = tegralDi {
            putFundef(::functionWithDefault)
        }
        val functionWrapper = env.getFundefOf(::functionWithDefault)
        assertDoesNotThrow { functionWrapper.checkCallable() }
    }

    @Test
    fun `Check callable successful with optionality (put)`() {
        val env = tegralDi {
            put(::functionWithDefault)
        }
        val functionWrapper = env.getFundefOf(::functionWithDefault)
        assertDoesNotThrow { functionWrapper.checkCallable() }
    }

    @Test
    fun `Instance provided when not an instance func`() {
        val env = tegralDi {
            putFundef(::functionWithDependency)
            put(::Greeter)
        }
        val functionWrapper = env.getFundefOf(::functionWithDependency)
        assertFailsWith<IllegalArgumentException> { functionWrapper.checkCallable(instance = "HELLO") }
    }

    @Test
    fun `Instance provided when not an instance func (put)`() {
        val env = tegralDi {
            put(::functionWithDependency)
            put(::Greeter)
        }
        val functionWrapper = env.getFundefOf(::functionWithDependency)
        assertFailsWith<IllegalArgumentException> { functionWrapper.checkCallable(instance = "HELLO") }
    }

    @Test
    fun `Extension provided when not an extension func`() {
        val env = tegralDi {
            putFundef(::functionWithDependency)
            put(::Greeter)
        }
        val functionWrapper = env.getFundefOf(::functionWithDependency)
        assertFailsWith<IllegalArgumentException> { functionWrapper.checkCallable(extension = "HELLO") }
    }

    @Test
    fun `Extension provided when not an extension func (put)`() {
        val env = tegralDi {
            put(::functionWithDependency)
            put(::Greeter)
        }
        val functionWrapper = env.getFundefOf(::functionWithDependency)
        assertFailsWith<IllegalArgumentException> { functionWrapper.checkCallable(extension = "HELLO") }
    }

    @Test
    fun `Actually working instance`() {
        val env = tegralDi {
            putFundef(SomeDumbClass::saySomething)
        }
        val functionWrapper = env.getFundefOf(SomeDumbClass::saySomething)
        val res = functionWrapper.invoke(instance = SomeDumbClass("HIHIHI"))
        assertEquals("Something HIHIHI", res)
    }

    @Test
    fun `Actually working instance (put)`() {
        val env = tegralDi {
            put(SomeDumbClass::saySomething)
        }
        val functionWrapper = env.getFundefOf(SomeDumbClass::saySomething)
        val res = functionWrapper.invoke(instance = SomeDumbClass("HIHIHI"))
        assertEquals("Something HIHIHI", res)
    }
}
