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

package guru.zoroark.tegral.di.environment.resolvers

import guru.zoroark.tegral.di.FailedToResolveException
import guru.zoroark.tegral.di.environment.Identifier
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class AliasResolverTest {
    class Target

    @Test
    fun `Resolving against unknown component`() {
        val resolver = AliasIdentifierResolver(Identifier(Target::class))
        assertFailsWith<FailedToResolveException>(
            message = "Failed to resolver guru.zoroark.tegral.di.environment.resolvers.AliasResolverTest.Target " +
                "(<no qualifier>) against environment. Make sure that what your alias is pointing to " +
                "(guru.zoroark.tegral.di.environment.resolvers.AliasResolverTest.Target (<no qualifier>)) actually " +
                "exists in the environment."
        ) {
            resolver.resolve(null, mapOf())
        }
    }

    @Test
    fun `Resolving normally`() {
        val identifier = Identifier(Target::class)
        val resolver = AliasIdentifierResolver(identifier)

        val mockTarget = mockk<Target>()
        val resolverMock = mockk<IdentifierResolver<Target>> {
            every { resolve(any(), any()) } returns mockTarget
        }

        val resolved = resolver.resolve(null, mapOf(identifier to resolverMock))
        assertSame(mockTarget, resolved)
    }
}
