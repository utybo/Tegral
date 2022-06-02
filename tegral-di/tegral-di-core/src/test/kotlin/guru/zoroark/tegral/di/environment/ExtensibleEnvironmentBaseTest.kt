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

package guru.zoroark.tegral.di.environment

import guru.zoroark.tegral.di.entryOf
import guru.zoroark.tegral.di.extensions.DeclarationsProcessor
import guru.zoroark.tegral.di.extensions.ExtensibleEnvironmentContext
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame

@Suppress("UnnecessaryAbstractClass")
abstract class ExtensibleEnvironmentBaseTest(
    private val provider: (ExtensibleEnvironmentContext) -> ExtensibleInjectionEnvironment
) : EnvironmentBaseTest({ ctx ->
    provider(ExtensibleEnvironmentContext(ctx.declarations, EnvironmentContext(mapOf())))
}) {
    class B
    class A(scope: InjectionScope) {
        val b: B by scope.meta()
    }

    class D
    class E
    class F

    class OptionalA
    class OptionalB(scope: InjectionScope) {
        val a: OptionalA? by scope.meta.optional()
    }

    @Test
    fun `(Extension) Injection pass-through to meta-environment`() {
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

    @Test
    fun `(Extension) getAllIdentifiers`() {
        val ctx = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { D() },
                entryOf(named("E")) { E() },
                entryOf { F() }
            ),
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

    @Test
    fun `(Extension) Environment injects itself within meta environment`() {
        val env = provider(ExtensibleEnvironmentContext(mapOf(), EnvironmentContext(mapOf())))
        assertSame(env, env.metaEnvironment.get<ExtensibleInjectionEnvironment>())
    }

    @Test
    fun `(Extension) Environment calls declaration processors`() {
        val processed = mutableListOf<Declaration<*>>()
        val processor = object : DeclarationsProcessor {
            override fun processDeclarations(sequence: Sequence<Declaration<*>>) {
                processed.addAll(sequence)
            }
        }
        provider(
            ExtensibleEnvironmentContext(
                mapOf(entryOf { "Hello" }),
                EnvironmentContext(
                    mapOf(entryOf { processor })
                )
            )
        )
        assertEquals(1, processed.size)
        assertEquals(Identifier(String::class), processed[0].identifier)
    }

    @Test
    fun `(Extension) Optional meta injection with present component should work`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { OptionalB(scope) }
            ),
            EnvironmentContext(
                mapOf(
                    entryOf { OptionalA() }
                )
            )
        )
        val env = provider(context)
        val aFromEnv = assertNotNull(env.get<OptionalB>().a)
        assertSame(env.metaEnvironment.get<OptionalA>(), aFromEnv)
    }

    @Test
    fun `(Extension) Optional meta injection with absent component should work`() {
        val context = ExtensibleEnvironmentContext(
            mapOf(
                entryOf { OptionalB(scope) }
            ),
            EnvironmentContext(mapOf())
        )
        val env = provider(context)
        assertNull(env.get<OptionalB>().a)
    }
}
