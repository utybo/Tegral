package guru.zoroark.tegral.openapi.ktor

import guru.zoroark.tegral.openapi.dsl.schema
import io.ktor.server.application.call
import io.ktor.server.locations.KtorExperimentalLocationsAPI
import io.ktor.server.locations.Location
import io.ktor.server.locations.Locations
import io.ktor.server.locations.get
import io.ktor.server.response.respond
import io.ktor.server.testing.testApplication
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.QueryParameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(KtorExperimentalLocationsAPI::class)
@Location("/foo")
data class FooBar(val example: String)

@OptIn(KtorExperimentalLocationsAPI::class)
// See also https://github.com/utybo/Koa/issues/5 / KTOR-4239
class Ktor4239Workaround {
    @Test
    fun `Test KTOR-4239 workaround`() = testApplication {
        install(TegralOpenApiKtor)
        install(Locations)
        routing {
            get<FooBar> {
                call.respond("Hello World!")
            } describe {
                summary = "Hello there"
                "example" queryParameter {
                    description = "Yep it's a parameter"
                    schema("hello")
                }
                200 response {
                    description = "The return value"
                    plainText { schema("yes") }
                }
            }
        }

        application {
            val document = openApi.buildOpenApiDocument()
            val expected = OpenAPI().apply {
                paths = Paths().addPathItem(
                    "/foo",
                    PathItem().apply {
                        operation(
                            PathItem.HttpMethod.GET,
                            Operation().apply {
                                summary = "Hello there"
                                responses = ApiResponses().addApiResponse(
                                    "200",
                                    ApiResponse().apply {
                                        description = "The return value"
                                        content = Content().addMediaType(
                                            "text/plain", MediaType().apply {
                                                schema = StringSchema()
                                                example = "yes"
                                            }
                                        )
                                    }
                                )
                                parameters = listOf(
                                    QueryParameter().apply {
                                        name = "example"
                                        description = "Yep it's a parameter"
                                        schema = StringSchema()
                                        example = "hello"
                                    }
                                )
                            }
                        )
                    }
                )
            }
            assertEquals(expected, document)
        }
    }
}
