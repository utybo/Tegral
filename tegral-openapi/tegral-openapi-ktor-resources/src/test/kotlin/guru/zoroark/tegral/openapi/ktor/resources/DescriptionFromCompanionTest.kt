package guru.zoroark.tegral.openapi.ktor.resources

import guru.zoroark.tegral.openapi.dsl.OperationBuilder
import guru.zoroark.tegral.openapi.dsl.OperationDsl
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

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
        val description = descriptionFromCompanionObject<CompanionObjectWithoutInterface>()
        TODO("What would be the appropriate reaction from the lib here?")
    }

    class NoCompanionObject

    @Test
    fun `No companion object`() {
        val description = descriptionFromCompanionObject<NoCompanionObject>()
        TODO("What would be the appropriate reaction from the lib here?")
    }
}
