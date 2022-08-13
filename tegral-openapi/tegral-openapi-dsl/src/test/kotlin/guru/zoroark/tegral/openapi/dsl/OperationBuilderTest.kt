package guru.zoroark.tegral.openapi.dsl

import io.mockk.mockk
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.parameters.CookieParameter
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement
import kotlin.test.Test
import kotlin.test.assertEquals

class OperationBuilderTest {
    @Test
    fun `Operation with all info`() {
        val operation = OperationBuilder(mockk()).apply {
            summary = "My operation"
            description = "My operation description"
            externalDocsDescription = "My external docs description"
            externalDocsUrl = "https://my.external.docs.url.example.com"
            deprecated = true
            operationId = "myOperation"
            security("sec-one")
            security("sec-two", "scope-a", "scope-b")
            tags += "tag-alpha"
            "pathParam" pathParameter {}
            "headerParam" headerParameter {}
            "queryParam" queryParameter {}
            "cookieParam" cookieParameter {}

            body {}
            200 response {}
        }.build()
        val expected = Operation().apply {
            summary = "My operation"
            description = "My operation description"
            externalDocs = ExternalDocumentation().apply {
                description = "My external docs description"
                url = "https://my.external.docs.url.example.com"
            }
            deprecated = true
            operationId = "myOperation"
            security = listOf(
                SecurityRequirement().apply {
                    addList("sec-one")
                },
                SecurityRequirement().apply {
                    addList("sec-two", listOf("scope-a", "scope-b"))
                }
            )
            tags = listOf("tag-alpha")
            parameters = listOf(
                PathParameter().name("pathParam"),
                HeaderParameter().name("headerParam"),
                QueryParameter().name("queryParam"),
                CookieParameter().name("cookieParam")
            )
            requestBody = RequestBody()
            responses = ApiResponses().apply {
                addApiResponse("200", ApiResponse())
            }
        }
        assertEquals(expected, operation)
    }

    @Test
    fun `Operation with no info`() {
        val operation = OperationBuilder(mockk()).build()
        val expected = Operation()
        assertEquals(expected, operation)
    }
}
