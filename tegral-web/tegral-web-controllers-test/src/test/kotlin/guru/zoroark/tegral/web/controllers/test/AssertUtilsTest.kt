package guru.zoroark.tegral.web.controllers.test

import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertFails

class AssertUtilsTest {
    private fun responseWithStatus(code: HttpStatusCode): HttpResponse {
        return mockk {
            every { status } returns code
        }
    }

    @Test
    fun `2xx assertion works with 200-like responses`() {
        for (code in listOf(HttpStatusCode.OK, HttpStatusCode.NoContent, HttpStatusCode.Created)) {
            val response = responseWithStatus(code)
            assertDoesNotThrow { assert2xx(response) }
        }
    }

    @Test
    fun `2xx assertion does not work with other responses`() {
        for (code in listOf(HttpStatusCode.Continue, HttpStatusCode.PermanentRedirect, HttpStatusCode.NotFound, HttpStatusCode.ServiceUnavailable)) {
            val response = responseWithStatus(code)
            assertFails { assert2xx(response) }
        }
    }

    @Test
    fun `3xx assertion works with 300-like responses`() {
        for (code in listOf(HttpStatusCode.TemporaryRedirect, HttpStatusCode.Found, HttpStatusCode.PermanentRedirect)) {
            val response = responseWithStatus(code)
            assertDoesNotThrow { assert3xx(response) }
        }
    }

    @Test
    fun `3xx assertion does not work with other responses`() {
        for (code in listOf(HttpStatusCode.Continue, HttpStatusCode.Created, HttpStatusCode.NotFound, HttpStatusCode.ServiceUnavailable)) {
            val response = responseWithStatus(code)
            assertFails { assert3xx(response) }
        }
    }

    @Test
    fun `4xx assertion works with 400-like responses`() {
        for (code in listOf(HttpStatusCode.NotFound, HttpStatusCode.Conflict, HttpStatusCode.BadRequest)) {
            val response = responseWithStatus(code)
            assertDoesNotThrow { assert4xx(response) }
        }
    }

    @Test
    fun `4xx assertion does not work with other responses`() {
        for (code in listOf(HttpStatusCode.Continue, HttpStatusCode.Created, HttpStatusCode.TemporaryRedirect, HttpStatusCode.ServiceUnavailable)) {
            val response = responseWithStatus(code)
            assertFails { assert4xx(response) }
        }
    }

    @Test
    fun `5xx assertion works with 500-like responses`() {
        for (code in listOf(HttpStatusCode.InternalServerError, HttpStatusCode.GatewayTimeout, HttpStatusCode.InsufficientStorage)) {
            val response = responseWithStatus(code)
            assertDoesNotThrow { assert5xx(response) }
        }
    }

    @Test
    fun `5xx assertion does not work with other responses`() {
        for (code in listOf(HttpStatusCode.Continue, HttpStatusCode.Created, HttpStatusCode.TemporaryRedirect, HttpStatusCode.NotFound)) {
            val response = responseWithStatus(code)
            assertFails { assert5xx(response) }
        }
    }
}
