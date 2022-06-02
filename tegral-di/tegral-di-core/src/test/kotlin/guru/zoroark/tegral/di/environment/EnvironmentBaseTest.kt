package guru.zoroark.tegral.di.environment

import guru.zoroark.tegral.di.AnotherElementClass
import guru.zoroark.tegral.di.AtoB
import guru.zoroark.tegral.di.BtoA
import guru.zoroark.tegral.di.ComponentNotFoundException
import guru.zoroark.tegral.di.ElementClass
import guru.zoroark.tegral.di.OtherElementClass
import guru.zoroark.tegral.di.entryOf
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@Suppress("UnnecessaryAbstractClass")
abstract class EnvironmentBaseTest(private val provider: (EnvironmentContext) -> InjectionEnvironment) {
    class A(scope: InjectionScope) {
        val c: C by scope()
        val b: B by scope()
    }

    class B(scope: InjectionScope) {
        val c: C by scope()
    }

    class C

    class D(scope: InjectionScope) {
        val e: E by scope()
        val eBis: E by scope(named("eBis"))
    }

    class E(scope: InjectionScope) {
        val f1: F by scope(Identifier(F::class, named("f1")))
        val f2: F by scope(named("f2"))
    }

    class F

    @Test
    fun `(Basic) Put and get a single element`() {
        var element: ElementClass? = null
        val context = EnvironmentContext(mapOf(entryOf { ElementClass().also { element = it } }))
        val env = provider(context)
        assertSame(element, env.get())
    }

    @Test
    fun `(Basic) Put and get multiple elements`() {
        var element: ElementClass? = null
        var otherElement: OtherElementClass? = null
        var anotherElement: AnotherElementClass? = null
        val context = EnvironmentContext(
            mapOf(
                entryOf {
                    assertNull(element, "Called builder twice!")
                    ElementClass().also { element = it }
                },
                entryOf {
                    assertNull(otherElement, "Called builder twice!")
                    OtherElementClass().also { otherElement = it }
                },
                entryOf {
                    assertNull(anotherElement, "Called builder twice!")
                    AnotherElementClass().also { anotherElement = it }
                }
            )
        )
        val env = provider(context)
        assertSame(element, env.get())
        assertSame(otherElement, env.get())
        assertSame(anotherElement, env.get())
    }

    @Test
    fun `(Basic) Put, get and inject multiple elements`() {
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

    @Test
    fun `(Basic) Put, get and inject multiple elements with qualifiers`() {
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

        assertSame(d, env.get<D>())
        assertSame(f1, env.get<F>(named("f1")))
        assertSame(f2, env.get<F>(named("f2")))
        assertSame(e, env.get<E>())
        assertSame(eBis, env.get<E>(named("eBis")))

        assertSame(d?.e, e)
        assertSame(d?.eBis, eBis)

        assertSame(e?.f1, f1)
        assertSame(e?.f2, f2)

        assertSame(eBis?.f1, f1)
        assertSame(eBis?.f2, f2)
    }

    @Test
    fun `(Basic) Objects are created eagerly`() {
        var wasFirstBuilt = false
        var wasSecondBuilt = false
        val context = EnvironmentContext(
            mapOf(
                entryOf { ElementClass().also { wasFirstBuilt = true } },
                entryOf { OtherElementClass().also { wasSecondBuilt = true } }
            )
        )
        provider(context)
        assertTrue(wasFirstBuilt)
        assertTrue(wasSecondBuilt)
    }

    @Test
    fun `(Basic) Getting unknown component should fail`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { ElementClass() }
            )
        )
        val env = provider(context)
        val ex = assertThrows<ComponentNotFoundException> {
            env.get<OtherElementClass>()
        }
        assertEquals(Identifier(OtherElementClass::class), ex.notFound)
    }

    @Test
    fun `(Basic) Injecting unknown component should fail`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { AtoB(scope) }
            )
        )
        val ex = assertThrows<ComponentNotFoundException> {
            val env = provider(context) // Eager envs will fail here
            val aToB = env.get<AtoB>()
            aToB.useB() // Lazy envs will fail here
        }
        assertEquals(Identifier(BtoA::class), ex.notFound)
    }

    class OptionalA
    class OptionalB(scope: InjectionScope) {
        val a: OptionalA? by scope.optional()
    }

    @Test
    fun `(Basic) Optional injection with present component should work`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { OptionalA() },
                entryOf { OptionalB(scope) }
            )
        )
        val env = provider(context)
        val aFromEnv = assertNotNull(env.get<OptionalB>().a)
        assertSame(env.get<OptionalA>(), aFromEnv)
    }

    @Test
    fun `(Basic) Optional injection with absent component should work`() {
        val context = EnvironmentContext(
            mapOf(
                entryOf { OptionalB(scope) }
            )
        )
        val env = provider(context)
        assertNull(env.get<OptionalB>().a)
    }
}
