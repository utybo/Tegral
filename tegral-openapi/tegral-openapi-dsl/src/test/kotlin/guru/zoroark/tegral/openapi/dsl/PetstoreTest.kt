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

@file:Suppress("CommentSpacing", "EnumEntryNameCase")

package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement
import kotlin.test.assertEquals

@Suppress("EnumNaming", "EnumEntryName")
enum class OrderStatus {
    placed, approved, delivered
}

// TODO XML metadata?

@XmlRootElement(name = "order")
data class Order(
    @get:Schema(example = "10")
    val id: Long,
    @get:Schema(example = "198772")
    val petId: Long,
    @get:Schema(example = "7")
    val quantity: Int,
    val shipDate: LocalDateTime,
    @get:Schema(description = "Order Status", example = "approved")
    val status: OrderStatus,
    val complete: Boolean
)

@XmlRootElement(name = "category")
data class Category(
    @get:Schema(example = "1")
    val id: Long,
    @get:Schema(example = "Dogs")
    val name: String
)

// TODO x-swagger-router-model
@XmlRootElement(name = "tag")
data class Tag(val id: Long, val name: String)

// TODO 'required' handling?
@Schema(defaultValue = "available")
@Suppress("EnumNaming", "EnumEntryName")
enum class PetStatus { available, pending, sold }

@XmlRootElement(name = "pet")
data class Pet(
    @get:Schema(example = "10")
    val id: Long,
    @get:Schema(example = "doggie", required = true)
    val name: String,
    val category: Category,
    @get:Schema(required = true)
    @get:XmlElementWrapper(name = "photoUrls")
    @get:XmlElement(name = "photoUrl")
    val photoUrls: List<String>,
    @get:XmlElementWrapper(name = "tags")
    val tags: List<Tag>,
    @get:Schema(description = "pet status in the store")
    val status: PetStatus
)

@XmlRootElement(name = "##default")
data class ApiResponse(val code: Int, val type: String, val message: String)

@Suppress("LongMethod")
fun petstore() = openApi {
    //#region Metadata

    "/v3" server { }

    description = """
        This is a sample Pet Store Server based on the OpenAPI 3.0 specification.  You can find out more about
        Swagger at [http://swagger.io](http://swagger.io). In the third iteration of the pet store, we've switched to ~
        the design first approach!
        You can now help us improve the API whether it's by making changes to the definition itself or to the code.
        That way, with time, we can improve the API in general, and expose some of the new features in OAS3.

        Some useful links:
        - [The Pet Store repository](https://github.com/swagger-api/swagger-petstore)
        - [The source API definition for the Pet Store](https://github.com/swagger-api/swagger-petstore/blob/master/src~
        /main/resources/openapi.yaml)
    """.trimIndent().replace("~\n", "")

    version = "1.0.11"
    title = "Swagger Petstore - OpenAPI 3.0"
    termsOfService = "http://swagger.io/terms/"

    contactEmail = "apiteam@swagger.io"

    licenseName = "Apache 2.0"
    licenseUrl = "http://www.apache.org/licenses/LICENSE-2.0.html"

    externalDocsUrl = "http://swagger.io"
    externalDocsDescription = "Find out more about Swagger"

    "petstore_auth" securityScheme {
        oauth2Type
        flows = OAuthFlows().implicit(
            OAuthFlow().apply {
                authorizationUrl = "https://petstore.swagger.io/oauth/authorize"
                scopes = Scopes().apply {
                    this["write:pets"] = "modify pets in your account"
                    this["read:pets"] = "read your pets"
                }
            }
        )
    }

    "api_key" securityScheme {
        apiKeyType
        inHeader
        name = "api_key"
    }

    //#endregion

    //#region Tags

    "pet" tag {
        description = "Everything about your Pets"
        externalDocsDescription = "Find out more"
        externalDocsUrl = "http://swagger.io"
    }

    "store" tag {
        description = "Access to Petstore orders"
        externalDocsDescription = "Find out more about our store"
        externalDocsUrl = "http://swagger.io"
    }

    "user" tag {
        description = "Operations about user"
    }

    //#endregion

    //#region Pet endpoints

    "/pet" {
        post {
            tags += "pet"
            summary = "Add a new pet to the store"
            description = "Add a new pet to the store"
            operationId = "addPet"

            security("petstore_auth", "write:pets", "read:pets")

            200 response {
                description = "Successful operation"
                (xml or json) { schema<Pet>() }
            }
            405 response {
                description = "Invalid input"
            }

            body {
                description = "Create a new pet in the store"
                required = true

                (json or xml or form) { schema<Pet>() }
            }
        }

        put {
            tags += "pet"
            summary = "Update an existing pet"
            description = "Update an existing pet by Id"
            operationId = "updatePet"

            security("petstore_auth", "write:pets", "read:pets")

            200 response {
                description = "Successful operation"
                (xml or json) { schema<Pet>() }
            }
            400 response {
                description = "Invalid ID supplied"
            }
            404 response {
                description = "Pet not found"
            }
            405 response {
                description = "Validation exception"
            }

            body {
                description = "Update an existent pet in the store"
                required = true

                (json or xml or form) { schema<Pet>() }
            }
        }
    }

    "/pet/findByStatus" get {
        tags += "pet"
        summary = "Finds Pets by status"
        description = "Multiple status values can be provided with comma separated strings"
        operationId = "findPetsByStatus"

        security("petstore_auth", "write:pets", "read:pets")

        200 response {
            description = "successful operation"
            (xml or json) { schema<List<Pet>>() }
        }
        400 response {
            description = "Invalid status value"
        }

        "status" queryParameter {
            description = "Status values that need to be considered for filter"
            required = false
            explode = true

            schema<PetStatus>()
        }
    }

    "/pet/findByTags" get {
        tags += "pet"
        summary = "Finds Pets by tags"
        description = "Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing."
        operationId = "findPetsByTags"

        security("petstore_auth", "write:pets", "read:pets")

        200 response {
            description = "successful operation"
            xml { schema<List<Pet>>() }
            json { schema<List<Pet>>() }
        }
        400 response {
            description = "Invalid tag value"
        }

        "tags" queryParameter {
            description = "Tags to filter by"
            explode = true
            required = false

            schema<List<String>>()
        }
    }

    "/pet/{petId}" {
        get {
            tags += "pet"
            summary = "Find pet by ID"
            description = "Returns a single pet"
            operationId = "getPetById"

            security("api_key")
            security("petstore_auth", "write:pets", "read:pets")

            "petId" pathParameter {
                description = "ID of pet to return"
                schema<Long>()
            }

            200 response {
                description = "successful operation"
                (xml or json) { schema<Pet>() }
            }
            400 response {
                description = "Invalid ID supplied"
            }
            404 response {
                description = "Pet not found"
            }
        }

        post {
            tags += "pet"
            summary = "Updates a pet in the store with form data"
            description = ""
            operationId = "updatePetWithForm"

            security("petstore_auth", "write:pets", "read:pets")

            "petId" pathParameter {
                description = "ID of pet that needs to be updated"
                schema<Long>()
            }

            "name" queryParameter {
                description = "Name of pet that needs to be updated"
                schema<String>()
            }

            "status" queryParameter {
                description = "Status of pet that needs to be updated"
                schema<String>()
            }

            405 response {
                description = "Invalid input"
            }
        }

        delete {
            tags += "pet"
            summary = "Deletes a pet"
            description = ""
            operationId = "deletePet"

            security("petstore_auth", "write:pets", "read:pets")

            400 response {
                description = "Invalid pet value"
            }

            // TODO seems like a mistake as this should be handled by the apiKey security?
            "api_key" headerParameter {
                schema<String>()
                description = ""
                required = false
            }

            "petId" pathParameter {
                description = "Pet id to delete"
                schema<Long>()
            }
        }
    }

    "/pet/{petId}/uploadImage" post {
        tags += "pet"
        summary = "uploads an image"
        description = ""
        operationId = "uploadFile"

        security("petstore_auth", "write:pets", "read:pets")

        "petId" pathParameter {
            description = "ID of pet to update"
            schema<Long>()
        }

        "additionalMetadata" queryParameter {
            description = "Additional Metadata"
            required = false
            schema<String>()
        }

        body {
            "application/octet-stream" content {
                schema = StringSchema().format("binary")
            }
        }

        200 response {
            description = "successful operation"
            json { schema<ApiResponse>() }
        }
    }

    //#endregion

    //#region Store endpoints

    "/store/inventory" get {
        tags += "store"
        summary = "Returns pet inventories by status"
        description = "Returns a map of status codes to quantities"
        operationId = "getInventory"

        // TODO x-swagger-route-controller

        security("api_key")

        200 response {
            description = "successful operation"
            json { schema<Map<PetStatus, Int>>() }
        }
    }

    "/store/order" post {
        tags += "store"
        summary = "Place an order for a pet"
        description = "Place a new order in the store"
        operationId = "placeOrder"

        // TODO x-swagger-route-controller

        200 response {
            description = "successful operation"
            json { schema<Order>() }
        }
        405 response {
            description = "Invalid input"
        }

        body {
            (json or xml or form) { schema<Order>() }
        }
    }

    "/store/order/{orderId}" {
        get {
            tags += "store"
            summary = "Find purchase order by ID"
            description =
                "For valid response try integer IDs with value <= 5 or > 10. Other values will generate exceptions."
            operationId = "getOrderById"

            "orderId" pathParameter {
                description = "ID of order that needs to be fetched"
                schema<Long>()
            }

            200 response {
                description = "successful operation"
                (xml or json) { schema<Order>() }
            }
            400 response {
                description = "Invalid ID supplied"
            }
            404 response {
                description = "Order not found"
            }
        }

        delete {
            tags += "store"
            summary = "Delete purchase order by ID"
            description = "For valid response try integer IDs with value < 1000. Anything above 1000 or nonintegers " +
                "will generate API errors"
            operationId = "deleteOrder"

            "orderId" pathParameter {
                description = "ID of the order that needs to be deleted"
                schema<Long>()
            }

            400 response {
                description = "Invalid ID supplied"
            }
            404 response {
                description = "Order not found"
            }
        }
    }

    //#endregion
}

class PetstoreTest {
    private fun loadPetstoreFromResources(): OpenAPI {
        val petstoreJson =
            PetstoreTest::class.java.getResourceAsStream("/petstore-simple.openapi.yaml")!!.bufferedReader().use {
                it.readText()
            }
        return Yaml.mapper().readValue(petstoreJson, OpenAPI::class.java)
    }

    @Test
    fun `Test produced object matches example yaml`() {
        val openApiObj = petstore()
        val expected = loadPetstoreFromResources()
        // For some reason the components are wrapped in an ObjectSchema when loaded, so we dump then load the 'actual'
        // value to have the same format
        val openApiObjYaml = Yaml.mapper().writeValueAsString(openApiObj)
        val actual = Yaml.mapper().readValue(openApiObjYaml, OpenAPI::class.java)

        // The output from assertEquals is *way* too big
        // Assert each part individually before checking everything to get more useful outputs
        assertEquals(expected.info, actual.info)
        assertEquals(expected.externalDocs, actual.externalDocs)
        assertEquals(expected.servers, actual.servers)
        assertEquals(expected.security, actual.security)
        assertEquals(expected.tags, actual.tags)
        assertEquals(expected.paths, actual.paths)
        assertEquals(expected.components, actual.components)

        // And also assert the entire thing (for real).
        assertEquals(expected, actual)
    }
}
