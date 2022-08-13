package guru.zoroark.tegral.openapi.ktorui

import io.ktor.client.request.get
import io.ktor.client.request.request
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.TestApplication
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TegralSwaggerUiKtorTest {
    private fun ApplicationTestBuilder.setup() {
        install(TegralSwaggerUiKtor) {}

        routing {
            swaggerUiEndpoint("/swagger", "/openapi/is/right/there")
        }
    }

    @Test
    fun `Redirect from base endpoint`() = testApplication {
        setup()
        val client = createClient {
            followRedirects = false
        }
        val response = client.get("/swagger")
        assertEquals(HttpStatusCode.Found, response.status)
        assertEquals("/swagger/index.html", response.headers["Location"])
    }

    @Test
    fun `Returns expected index content`() = testApplication {
        setup()
        val response = client.get("/swagger/index.html")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("<body>"))
    }

    @Test
    fun `Returns expected index content with only trailing slash`() = testApplication {
        setup()
        val response = client.get("/swagger/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("<body>"))
    }

    @Test
    fun `Returns 404 when swagger resource not found`() = testApplication {
        setup()
        val response = client.get("/swagger/idonotexist")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `Returns swizzled initialize`() = testApplication {
        setup()

        val response = client.get("/swagger/swagger-initializer.js")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(response.bodyAsText().contains("url: \"/openapi/is/right/there\""), true)
    }
}
