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

import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EnvironmentContext
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.environment.named
import guru.zoroark.tegral.di.environment.optional
import guru.zoroark.tegral.di.environment.resolvers.SimpleIdentifierResolver
import guru.zoroark.tegral.di.extensions.DeclarationsProcessor
import guru.zoroark.tegral.di.extensions.ExtensibleEnvironmentContext
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import guru.zoroark.tegral.di.test.entryOf
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * An [EnvironmentBaseTest] specifically for extensible environments.
 *
 * **Remember to have at least one test that executes [runTests], see [EnvironmentBaseTest]'s documentation for more
 * information.
 */
@Suppress("UnnecessaryAbstractClass", "FunctionName")
abstract class ExtensibleEnvironmentBaseTest(
    private val provider: (ExtensibleEnvironmentContext) -> ExtensibleInjectionEnvironment
) : EnvironmentBaseTest({ ctx ->
    provider(ExtensibleEnvironmentContext(ctx.declarations, EnvironmentContext(mapOf())))
}) {
    final override val additionalTests: List<Pair<String, () -> Unit>>
        get() = listOf(
            "(Extension) Injection pass-through to meta-environment" to
                this::`(Extension) Injection pass-through to meta-environment`,
            "(Extension) getAllIdentifiers" to
                this::`(Extension) getAllIdentifiers`,
            "(Extension) Environment injects itself within meta environment" to
                this::`(Extension) Environment injects itself within meta environment`,
            "(Extension) Environment calls declaration processors" to
                this::`(Extension) Environment calls declaration processors`,
            "(Extension) Optional meta injection with present component should work" to
                this::`(Extension) Optional meta injection with present component should work`,
            "(Extension) Optional meta injection with absent component should work" to
                this::`(Extension) Optional meta injection with absent component should work`,
            "(Extension) Get resolver or null, resolver present" to
                this::`(Extension) Get resolver or null, resolver present`,
            "(Extension) Get resolver or null, resolver absent" to
                this::`(Extension) Get resolver or null, resolver absent`,
        )

    private class B
    private class A(scope: InjectionScope) {
        val b: B by scope.meta()
    }

    private class D
    private class E
    private class F

    private class OptionalA
    private class OptionalB(scope: InjectionScope) {
        val a: OptionalA? by scope.meta.optional()
    }

    private fun `(Extension) Injection pass-through to meta-environment`() {
        var a: A? = null
        var b: B? = null
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf {
                    assertNull(a, "Called builder twice!")
                    A(scope).also { a = it }
                }
            ),
            EnvironmentContext(
                mapOf(
                    entryOf {
                        assertNull(b, "Called builder twice!")
                        B().also { b = it }
                    }
                )
            )
        )
        val env = provider(context)
        assertSame(a, env.get())
        assertSame(b, env.metaEnvironment.get())
        assertSame(a?.b, b)
    }

    private fun `(Extension) getAllIdentifiers`() {
        val ctx = ExtensibleEnvironmentContext(
            mapOf(entryOf { D() }, entryOf(named("E")) { E() }, entryOf { F() }),
            EnvironmentContext(mapOf())
        )
        val expectedIdentifiers = setOf(
            Identifier(D::class),
            Identifier(E::class, named("E")),
            Identifier(F::class)
        )
        val env = provider(ctx)
        assertEquals(expectedIdentifiers, env.getAllIdentifiers().toSet())
    }

    private fun `(Extension) Environment injects itself within meta environment`() {
        val env = provider(ExtensibleEnvironmentContext(mapOf(), EnvironmentContext(mapOf())))
        assertSame(env, env.metaEnvironment.get())
    }

    private fun `(Extension) Environment calls declaration processors`() {
        val processed = mutableListOf<Declaration<*>>()
        val processor = object : DeclarationsProcessor {
            override fun processDeclarations(sequence: Sequence<Declaration<*>>) {
                processed.addAll(sequence)
            }
        }
        provider(
            ExtensibleEnvironmentContext(
                mapOf(entryOf { "Hello" }),
                EnvironmentContext(mapOf(entryOf { processor }))
            )
        )
        assertEquals(1, processed.size)
        assertEquals(Identifier(String::class), processed[0].identifier)
    }

    private fun `(Extension) Optional meta injection with present component should work`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(entryOf { OptionalB(scope) }),
            EnvironmentContext(mapOf(entryOf { OptionalA() }))
        )
        val env = provider(context)
        val aFromEnv = assertNotNull(env.get<OptionalB>().a)
        assertSame(env.metaEnvironment.get(), aFromEnv)
    }

    private fun `(Extension) Optional meta injection with absent component should work`() {
        val context = ExtensibleEnvironmentContext(mapOf(entryOf { OptionalB(scope) }), EnvironmentContext(mapOf()))
        val env = provider(context)
        assertNull(env.get<OptionalB>().a)
    }

    private fun `(Extension) Get resolver or null, resolver present`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { B() }
            ),
            EnvironmentContext(emptyMap())
        )
        val env = provider(context)
        val resolver = env.getResolverOrNull(Identifier(B::class))
        assertNotNull(resolver)
        assertIs<SimpleIdentifierResolver<B>>(resolver)
        val b = resolver.resolve(null, emptyMap())
        assertIs<B>(b)
    }

    private fun `(Extension) Get resolver or null, resolver absent`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { B() }
            ),
            EnvironmentContext(emptyMap())
        )
        val env = provider(context)
        val resolver = env.getResolverOrNull(Identifier(A::class))
        assertNull(resolver)
    }
}
