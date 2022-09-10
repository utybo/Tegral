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

package guru.zoroark.tegral.openapi.ktor.resources

import guru.zoroark.tegral.openapi.dsl.OperationBuilder
import guru.zoroark.tegral.openapi.dsl.OperationDsl
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Suppress("UtilityClassWithPublicConstructor")
class DescriptionFromCompanionTest {
    class UsingManualImplementation {
        companion object : ResourceDescription {
            override val openApi: OperationDsl.() -> Unit = {
                description = "Howdy!"
            }
        }
    }

    @Test
    fun `Manual companion object implementation`() {
        val description = descriptionFromCompanionObject<UsingManualImplementation>()
        val result = OperationBuilder(mockk()).apply(description)
        assertEquals("Howdy!", result.description)
    }

    class UsingDelegation {
        companion object : ResourceDescription by describeResource({
            description = "Hey!"
        })
    }

    @Test
    fun `Companion object implementation via delegation`() {
        val description = descriptionFromCompanionObject<UsingDelegation>()
        val result = OperationBuilder(mockk()).apply(description)
        assertEquals("Hey!", result.description)
    }

    class CompanionObjectWithoutInterface {
        companion object
    }

    @Test
    fun `Companion object without interface`() {
        // Should just be a no-op
        val description = descriptionFromCompanionObject<CompanionObjectWithoutInterface>()
        val result = OperationBuilder(mockk()).apply(description)
        assertNull(result.description)
    }

    class NoCompanionObject

    @Test
    fun `No companion object`() {
        // Should just be a no-op
        val description = descriptionFromCompanionObject<NoCompanionObject>()
        val result = OperationBuilder(mockk()).apply(description)
        assertNull(result.description)
    }
}
