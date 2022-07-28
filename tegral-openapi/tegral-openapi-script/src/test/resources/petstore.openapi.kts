//#region Metadata

servers {
    "/v3" { }
}

info {
    description = """
        This is a sample Pet Store Server based on the OpenAPI 3.0 specification.  You can find out more about
        Swagger at [http://swagger.io](http://swagger.io). In the third iteration of the pet store, we've switched to the design first approach!
        You can now help us improve the API whether it's by making changes to the definition itself or to the code.
        That way, with time, we can improve the API in general, and expose some of the new features in OAS3.
        Some useful links:
        - [The Pet Store repository](https://github.com/swagger-api/swagger-petstore)
        - [The source API definition for the Pet Store](https://github.com/swagger-api/swagger-petstore/blob/master/src/main/resources/openapi.yaml)
    """

    version = "1.0.11"
    title = "Swagger Petstore - OpenAPI 3.0"
    termsOfService = "http://swagger.io/terms/"

    contact {
        email = "apiteam@swagger.io"
    }

    license {
        name = "Apache 2.0"
        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
    }
}

//#endregion

//#region Tags

"pet" tag {
    description = "Everything about your Pets"
    externalDocs {
        description = "Find out more"
        url = "http://swagger.io"
    }
}

"store" tag {
    description = "Access to Petstore orders"
    externalDocs { 
        description = "Find out more about our store"
        url = "http://swagger.io"
    }
}

"user" tag {
    description = "Operations about user"
}

//#endregion

//#region Schemas

enum class OrderStatus {
    placed,
    approved,
    delivered
}

// TODO XML metadata?

data class Order(
    val id: Long,
    val petId: Long,
    val quantity: Int,
    val shipDate: DateTime,
    val status: OrderStatus,
    val complete: Boolean
)
val orderExample = Order(
    id = 10,
    petId = 198772,
    quantity = 7,
    shipDate = DateTime.now(),
    status = OrderStatus.approved,
    complete = false
)

data class Address(
    street: String,
    city: String,
    state: String,
    zip: String
)
val addressExample = Address(
    street = "437 Lytton",
    city = "Palo Alto",
    state = "CA",
    zip = "94301"
)

data class Customer(
    id: Long,
    example: Long,
    username: String,
    address: List<Address>
)
val customerExample = Customer(
    id = 10,
    example = 10,
    username = "jdoe",
    address = listOf(addressExample)
)

data class Category(val id: Long, val name: String)
val categoryExample = Category(1, "Dogs")

data class User(
    val id: Long,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String,
    // TODO Field description
    val userStatus: Int
)
val userExample = User(10, "theUser", "John", "James", "john@email.com", "12345", "12345", 1)

// TODO x-swagger-router-model
data class Tag(id: Long, name: String)

// TODO 'required' handling?
enum class PetStatus { available, pending, sold }
data class Pet(
    val id: Long,
    val name: String,
    val category: Category,
    val photoUrls: List<String>,
    val tags: List<Tag>,
    val status: PetStatus
)
val petExample = Pet(10, "doggie", categoryExample, listOf())

data class ApiResponse(code: Int, type: String, message: String)

//#endregion


//#region Pet endpoints

"/pet" {
    post {
        tags += "pet"
        summary = "Add a new pet to the store"
        description = "Add a new pet to the store"
        operationId = "addPet"

        security { "petstore_auth"("write:pets", "read:pets") }

        200 response {
            description = "Successful operation"
            json { schema(petExample) }
            xml { schema(petExample) }
            // TODO possible shorthand?
            // json or xml { schema(petExample) }
        }
        405 response {
            description = "Invalid input"
        }

        body {
            description = "Create a new pet in the store"
            required()

            json { schema(petExample) }
            xml { schema(petExample) }
            urlEncoded { schema(petExample) }
            // json or xml or urlEncoded { schema(petExample) }
        }
    }

    put {
        tags += "pet"
        summary = "Update an existing pet"
        description = "Update an existing pet"
        operationId = "updatePet"

        security { "petstore_auth"("write:pets", "read:pets") }

        200 response {
            description = "Successful operation"
            json { schema(petExample) }
            xml { schema(petExample) }
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
            required()

            json { schema(petExample) }
            xml { schema(petExample) }
            urlEncoded { schema(petExample) }
            // (json or xml or urlEncoded) { schema(petExample) }
        }
    }
}

"/pet/findByStatus" get {
    tags += "pet"
    summary = "Finds Pets by status"
    description = "Multiple status values can be provided with comma separated strings"
    operationId = "findPetsByStatus"

    security { "petstore_auth"("write:pets", "read:pets") }

    200 response {
        description = "successful operation"
        json { schema(petExample) }
        xml { schema(petExample) }
    }
    400 response {
        description = "Invalid status value"
    }

    "status" queryParameter {
        description = "Status values that need to be considered for filter"
        required()
        
        schema<PetStatus>()
    }
}

"/pet/findByTags" get {
    tags += "pet"
    summary = "Finds Pets by tags"
    description = "Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing."
    operationId = "findPetsByTags"

    security { "petstore_auth"("write:pets", "read:pets") }

    200 response {
        description = "successful operation"
        json { schema<List<Pet>>() }
        xml { schema<List<Pet>>() }
    }
    400 response {
        description = "Invalid tag value"
    }

    "tags" queryParameter {
        description = "Tags to filter by"

        schema<String>()
    }
}

"/pet/{petId}" {
    get {
        tags += "pet"
        summary = "Find pet by ID"
        description = "Returns a single pet"
        operationId = "getPetById"

        security { "petstore_auth"("write:pets", "read:pets") }
        security += "api_key"

        "petId" pathParameter {
            description = "ID of pet to return"
            schema<Long>()
        }

        200 response {
            description = "successful operation"
            json { schema(petExample) }
            xml { schema(petExample) }
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
        operationId = "updatePetWithForm"

        security { "petstore_auth"("write:pets", "read:pets") }

        "petId" pathParameter {
            description = "ID of pet that needs to be updated"
            schema<Long>()
        }

        "name" queryParameter {
            description = "Updated name of the pet"
            schema<String>()
        }

        "status" queryParameter {
            description = "Updated status of the pet"
            schema<PetStatus>()
        }

        405 response {
            description = "Invalid input"
        }
    }

    delete {
        tags += "pet"
        summary = "Deletes a pet"
        operationId = "deletePet"

        security { "petstore_auth"("write:pets", "read:pets") }

        400 response {
            description = "Invalid ID supplied"
        }

        // TODO seems like a mistake as this should be handled by the apiKey security?
        "api_key" headerParameter {
            schema<String>()
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
    operationId = "uploadFile"

    security { "petstore_auth"("write:pets", "read:pets") }

    "petId" pathParameter {
        description = "ID of pet to update"
        schema<Long>()
    }

    "additionalMetadata" queryParameter {
        description = "Additional data to pass to server"
        schema<String>()
    }

    // TODO Name is not great
    // TODO Automatically sets schema to string with binary format
    binary body { }

    200 response {
        description = "successful operation"
        json { schema(petExample) }
        xml { schema(petExample) }
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
}

//#endregion

//#region Store endpoints

"/store/inventory" get {
    tags += "store"
    summary = "Returns pet inventories by status"
    description = "Returns a map of status codes to quantities"
    operationId = "getInventory"

    // TODO x-swagger-route-controller

    security { "api_key" }

    200 response {
        description = "successful operation"
        json { schema<Map<PetStatus, Long>>() }
    }
}

"/store/order" post {
    tags += "store"
    summary = "Place an order for a pet"
    description = ""
    operationId = "placeOrder"

    // TODO x-swagger-route-controller

    200 response {
        description = "successful operation"
        json { schema(orderExample) }
    }
    405 response {
        description = "Invalid input"
    }

    body {
        description = "Order to place for the pet"

        json { schema(orderExample) }
        xml { schema(orderExample) }
        urlEncoded { schema(orderExample) }
    }
}

"/store/order/{orderId}" {
    get {
        tags += "store"
        summary = "Find purchase order by ID"
        description = "For valid response try integer IDs with value >= 1 and <= 10. Other values will generated exceptions"
        operationId = "getOrderById"

        "orderId" pathParameter {
            description = "ID of pet that needs to be fetched"
            schema<Long>()
        }

        200 response {
            description = "successful operation"
            json { schema(orderExample) }
            xml { schema(orderExample) }
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
        description = "For valid response try integer IDs with value < 1000. Anything above 1000 or nonintegers will generate API errors"
        operationId = "deleteOrder"

        "orderId" pathParameter {
            description = "ID of order that needs to be fetched"
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

// TODO User endpoints
