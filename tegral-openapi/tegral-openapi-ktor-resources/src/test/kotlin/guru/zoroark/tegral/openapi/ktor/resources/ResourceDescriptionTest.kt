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

import guru.zoroark.tegral.openapi.dsl.OpenApiDslContext
import guru.zoroark.tegral.openapi.ktor.EndpointDescriptionHook
import io.ktor.resources.Resource
import io.mockk.mockk
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.PathParameter
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertEquals

@Resource("/thing")
@Serializable
class Thing {
    companion object : ResourceDescription by describeResource({
        tags += "stuff"

        get {
            summary = "Get all of the things"
        }

        post {
            summary = "Create a thing"
        }
    })

    @Resource("{id}")
    @Serializable
    class WithId(val id: Int, val parent: Thing = Thing()) {
        companion object : ResourceDescription by describeResource({
            tags += "morestuff"
            "id" pathParameter {
                description = "The ID of the thing"
            }

            get {
                summary = "Get the thing with the id"
            }

            put {
                summary = "Edit the thing with the id"
            }
        })
    }

    @Resource("yeet")
    @Serializable
    class Yeet(val parent: WithId) {
        companion object : ResourceDescription by describeResource({
            tags += "yeet"
            post {
                summary = "Satellize the thing"
            }
        })
    }
}

@Resource("/nm")
class ResourceWithoutMethodsSpecified(val test: String) {
    companion object : ResourceDescription by describeResource({
        summary = "Hello"
        description = "there"
    })
}

@Resource("/ov")
class ResourceWithOverrides(val test: String) {
    companion object : ResourceDescription by describeResource({
        summary = "Not me!"
        get { summary = "Me me me!" }
        post { summary = "Me me me!" }
        put { summary = "Me me me!" }
        delete { summary = "Me me me!" }
        patch { summary = "Me me me!" }
        options { summary = "Me me me!" }
        head { summary = "Me me me!" }
    })
}

@Resource("/nd")
class ResourceWithoutDescription(val test: String)

@Resource("/levels")
class ResourceWithLevelsOfHooks(val test: String) {
    companion object : ResourceDescription by describeResource({
        tags += "2"
        get { tags += "3" }
    })
}

class ResourceDescriptionTest {
    private inline fun <reified T : Any> createOperationsForClass(
        hooks: List<EndpointDescriptionHook> = emptyList(),
        noinline opSelector: OperationSelector
    ): Operation {
        val context = mockk<OpenApiDslContext>()
        val operation = descriptionFromResource<T>(opSelector)(context, hooks)
        return operation.build()
    }

    @Test
    fun `Not nested, get`() {
        val op = createOperationsForClass<Thing> { get }
        assertEquals(
            Operation().apply {
                summary = "Get all of the things"
                tags(listOf("stuff"))
            },
            op
        )
    }

    @Test
    fun `Not nested, post`() {
        val op = createOperationsForClass<Thing> { post }
        assertEquals(
            Operation().apply {
                summary = "Create a thing"
                tags(listOf("stuff"))
            },
            op
        )
    }

    @Test
    fun `Single nested, get`() {
        val op = createOperationsForClass<Thing.WithId> { get }
        assertEquals(
            Operation().apply {
                summary = "Get the thing with the id"
                tags(listOf("stuff", "morestuff"))
                addParametersItem(
                    PathParameter().apply {
                        name = "id"
                        description = "The ID of the thing"
                    }
                )
            },
            op
        )
    }

    @Test
    fun `Single nested, put`() {
        val op = createOperationsForClass<Thing.WithId> { put }
        assertEquals(
            Operation().apply {
                summary = "Edit the thing with the id"
                tags(listOf("stuff", "morestuff"))
                addParametersItem(
                    PathParameter().apply {
                        name = "id"
                        description = "The ID of the thing"
                    }
                )
            },
            op
        )
    }

    @Test
    fun `Single nested, post that should be fully default`() {
        val op = createOperationsForClass<Thing.WithId> { post }
        assertEquals(
            Operation().apply {
                tags(listOf("stuff", "morestuff"))
                addParametersItem(
                    PathParameter().apply {
                        name = "id"
                        description = "The ID of the thing"
                    }
                )
            },
            op
        )
    }

    @Test
    fun `Yeetly nested, post`() {
        val op = createOperationsForClass<Thing.Yeet> { post }
        assertEquals(
            Operation().apply {
                summary = "Satellize the thing"
                tags(listOf("stuff", "morestuff", "yeet"))
                addParametersItem(
                    PathParameter().apply {
                        name = "id"
                        description = "The ID of the thing"
                    }
                )
            },
            op
        )
    }

    @Test
    fun `Can use any method without it being specified by default`() {
        val list: List<OperationSelector> =
            listOf({ get }, { post }, { put }, { delete }, { patch }, { options }, { head })
        for (method in list) {
            val op = createOperationsForClass<ResourceWithoutMethodsSpecified>(opSelector = method)
            assertEquals(
                Operation().apply {
                    summary = "Hello"
                    description = "there"
                },
                op
            )
        }
    }

    @Test
    fun `Properly overrides in all cases`() {
        val list: List<OperationSelector> =
            listOf({ get }, { post }, { put }, { delete }, { patch }, { options }, { head })
        for (method in list) {
            val op = createOperationsForClass<ResourceWithOverrides>(opSelector = method)
            assertEquals(
                Operation().apply {
                    summary = "Me me me!"
                },
                op
            )
        }
    }

    @Test
    fun `Can have an empty thing but it still works`() {
        assertDoesNotThrow {
            createOperationsForClass<ResourceWithoutDescription> { get }
        }
    }

    @Test
    fun `Test override ordering`() {
        val op = createOperationsForClass<ResourceWithLevelsOfHooks>(listOf { tags += "1" }) { get }
        assertEquals(listOf("1", "2", "3"), op.tags)
    }
}
