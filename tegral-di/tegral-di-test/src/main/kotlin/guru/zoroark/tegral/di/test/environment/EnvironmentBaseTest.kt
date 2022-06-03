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

package guru.zoroark.tegral.di.test.environment

import guru.zoroark.tegral.di.ComponentNotFoundException
import guru.zoroark.tegral.di.environment.EnvironmentContext
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.environment.named
import guru.zoroark.tegral.di.environment.optional
import guru.zoroark.tegral.di.test.entryOf
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * A base test class that provides basic tests that any environment should respect.
 *
 * **This class DOES NOT run tests merely by subclassing it. Create a test of your own in your subclass that runs
 * [runTests].** For example:
 *
 * ```kotlin
 * class TestEagerEnvironment : NotExtensibleEnvironmentBaseTest(::EagerImmutableMetaEnvironment) {
 *     @Test
 *     fun `Base tests`() {
 *         runTests()
 *     }
 * }
 * ```
 *
 * You should consider using [NotExtensibleEnvironmentBaseTest] or [ExtensibleEnvironmentBaseTest] depending on which
 * environment you wish to test.
 */
@Suppress("UnnecessaryAbstractClass", "FunctionName")
sealed class EnvironmentBaseTest(
    private val provider: (EnvironmentContext) -> InjectionEnvironment
) {
    /**
     * Additional tests to run along with the default tests in [runTests]
     */
    abstract val additionalTests: List<Pair<String, () -> Unit>>

    /**
     * Runs all the basic tests from the base environment test, plus any additional tests defined in [additionalTests]
     */
    fun runTests() {
        val tests = listOf<Pair<String, () -> Unit>>(
            "(Basic) Put and get a single element" to
                this::`(Basic) Put and get a single element`,
            "(Basic) Put and get multiple elements" to
                this::`(Basic) Put and get multiple elements`,
            "(Basic) Put, get and inject multiple elements" to
                this::`(Basic) Put, get and inject multiple elements`,
            "(Basic) Put, get and inject multiple elements with qualifiers" to
                this::`(Basic) Put, get and inject multiple elements with qualifiers`,
            "(Basic) Objects are created eagerly" to
                this::`(Basic) Objects are created eagerly`,
            "(Basic) Getting unknown component should fail" to
                this::`(Basic) Getting unknown component should fail`,
            "(Basic) Injecting unknown component should fail" to
                this::`(Basic) Injecting unknown component should fail`,
            "(Basic) Optional injection with absent component should work" to
                this::`(Basic) Optional injection with absent component should work`,
            "(Basic) Optional injection with present component should work" to
                this::`(Basic) Optional injection with present component should work`,
        ) + additionalTests
        val failingTests = tests.mapNotNull { (name, test) ->
            val result = runCatching { test() }
            if (result.isSuccess) {
                println("OK: $name")
                null
            } else {
                val exception = result.exceptionOrNull()!!
                name to exception
            }
        }
        if (failingTests.isNotEmpty()) {
            failingTests.forEach { (name, exception) ->
                System.err.println()
                System.err.println("KO: $name FAILED")
                exception.printStackTrace()
            }
            fail("Multiple failing tests. Refer to the log output for details.")
        }
    }

    private class Simple
    private class SimpleTwo
    private class SimpleThree

    private class A(scope: InjectionScope) {
        val c: C by scope()
        val b: B by scope()
    }

    private class B(scope: InjectionScope) {
        val c: C by scope()
    }

    private class C

    private class D(scope: InjectionScope) {
        val e: E by scope()
        val eBis: E by scope(named("eBis"))
    }

    private class E(scope: InjectionScope) {
        val f1: F by scope(Identifier(F::class, named("f1")))
        val f2: F by scope(named("f2"))
    }

    private class F

    private fun `(Basic) Put and get a single element`() {
        var element: Simple? = null
        val context = EnvironmentContext(mapOf(entryOf { Simple().also { element = it } }))
        val env = provider(context)
        assertSame(element, env.get())
    }

    private fun `(Basic) Put and get multiple elements`() {
        var element: Simple? = null
        var otherElement: SimpleTwo? = null
        var anotherElement: SimpleThree? = null
        val context = EnvironmentContext(
            mapOf(
                entryOf {
                    assertNull(element, "Called builder twice!")
                    Simple().also { element = it }
                },
                entryOf {
                    assertNull(otherElement, "Called builder twice!")
                    SimpleTwo().also { otherElement = it }
                },
                entryOf {
                    assertNull(anotherElement, "Called builder twice!")
                    SimpleThree().also { anotherElement = it }
                }
            )
        )
        val env = provider(context)
        assertSame(element, env.get())
        assertSame(otherElement, env.get())
        assertSame(anotherElement, env.get())
    }

    private fun `(Basic) Put, get and inject multiple elements`() {
        var a: A? = null
        var b: B? = null
        var c: C? = null
        val context = EnvironmentContext(
            mapOf(
                entryOf {
                    assertNull(a, "Called builder twice!")
                    A(scope).also { a = it }
                },
                entryOf {
                    assertNull(b, "Called builder twice!")
                    B(scope).also { b = it }
                },
                entryOf {
                    assertNull(c, "Called builder twice!")
                    C().also { c = it }
                }
            )
        )
        val env = provider(context)
        assertSame(a, env.get())
        assertSame(a?.b, env.get())
        assertSame(a?.b?.c, env.get())
        assertSame(b, env.get())
        assertSame(b?.c, env.get())
        assertSame(c, env.get())
    }

    private fun `(Basic) Put, get and inject multiple elements with qualifiers`() {
        var d: D? = null
        var e: E? = null
        var eBis: E? = null
        var f1: F? = null
        var f2: F? = null

        val context = EnvironmentContext(
            mapOf(
                entryOf {
                    assertNull(d, "Called builder twice!")
                    D(scope).also { d = it }
                },
                entryOf(named("f1")) {
                    assertNull(f1, "Called builder twice!")
                    F().also { f1 = it }
                },
                entryOf(named("f2")) {
                    assertNull(f2, "Called builder twice!")
                    F().also { f2 = it }
                },
                entryOf {
                    assertNull(e, "Called builder twice!")
                    E(scope).also { e = it }
                },
                entryOf(named("eBis")) {
                    assertNull(eBis, "Called builder twice!")
                    E(scope).also { eBis = it }
                }
            )
        )
        val env = provider(context)
        assertNotNull(f1)
        assertNotNull(f2)
        assertNotNull(e)
        assertNotNull(eBis)

        assertSame(d, env.get())
        assertSame(f1, env.get(named("f1")))
        assertSame(f2, env.get(named("f2")))
        assertSame(e, env.get())
        assertSame(eBis, env.get(named("eBis")))

        assertSame(d?.e, e)
        assertSame(d?.eBis, eBis)

        assertSame(e?.f1, f1)
        assertSame(e?.f2, f2)

        assertSame(eBis?.f1, f1)
        assertSame(eBis?.f2, f2)
    }

    private fun `(Basic) Objects are created eagerly`() {
        var wasFirstBuilt = false
        var wasSecondBuilt = false
        val context = EnvironmentContext(
            mapOf(
                entryOf { Simple().also { wasFirstBuilt = true } },
                entryOf { SimpleTwo().also { wasSecondBuilt = true } }
            )
        )
        provider(context)
        assertTrue(wasFirstBuilt)
        assertTrue(wasSecondBuilt)
    }

    private fun `(Basic) Getting unknown component should fail`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { Simple() }
            )
        )
        val env = provider(context)
        val ex = assertFailsWith<ComponentNotFoundException> {
            env.get<SimpleTwo>()
        }
        assertEquals(Identifier(SimpleTwo::class), ex.notFound)
    }

    private fun `(Basic) Injecting unknown component should fail`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { B(scope) }
            )
        )
        val ex = assertFailsWith<ComponentNotFoundException> {
            val env = provider(context) // Eager envs will fail here
            val b = env.get<B>()
            b.c // Lazy envs will fail here
        }
        assertEquals(Identifier(C::class), ex.notFound)
    }

    private class OptionalA
    private class OptionalB(scope: InjectionScope) {
        val a: OptionalA? by scope.optional()
    }

    private fun `(Basic) Optional injection with present component should work`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { OptionalA() },
                entryOf { OptionalB(scope) }
            )
        )
        val env = provider(context)
        val aFromEnv = assertNotNull(env.get<OptionalB>().a)
        assertSame(env.get(), aFromEnv)
    }

    private fun `(Basic) Optional injection with absent component should work`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { OptionalB(scope) }
            )
        )
        val env = provider(context)
        assertNull(env.get<OptionalB>().a)
    }
}
