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
        listOf(
            HttpStatusCode.OK,
            HttpStatusCode.NoContent,
            HttpStatusCode.Created
        ).forEach { code ->
            val response = responseWithStatus(code)
            assertDoesNotThrow { assert2xx(response) }
        }
    }

    @Test
    fun `2xx assertion does not work with other responses`() {
        listOf(
            HttpStatusCode.Continue,
            HttpStatusCode.PermanentRedirect,
            HttpStatusCode.NotFound,
            HttpStatusCode.ServiceUnavailable
        ).forEach { code ->
            val response = responseWithStatus(code)
            assertFails { assert2xx(response) }
        }
    }

    @Test
    fun `3xx assertion works with 300-like responses`() {
        listOf(
            HttpStatusCode.TemporaryRedirect,
            HttpStatusCode.Found,
            HttpStatusCode.PermanentRedirect
        ).forEach { code ->
            val response = responseWithStatus(code)
            assertDoesNotThrow { assert3xx(response) }
        }
    }

    @Test
    fun `3xx assertion does not work with other responses`() {
        listOf(
            HttpStatusCode.Continue,
            HttpStatusCode.Created,
            HttpStatusCode.NotFound,
            HttpStatusCode.ServiceUnavailable
        ).forEach { code ->
            val response = responseWithStatus(code)
            assertFails { assert3xx(response) }
        }
    }

    @Test
    fun `4xx assertion works with 400-like responses`() {
        listOf(
            HttpStatusCode.NotFound,
            HttpStatusCode.Conflict,
            HttpStatusCode.BadRequest
        ).forEach { code ->
            val response = responseWithStatus(code)
            assertDoesNotThrow { assert4xx(response) }
        }
    }

    @Test
    fun `4xx assertion does not work with other responses`() {
        listOf(
            HttpStatusCode.Continue,
            HttpStatusCode.Created,
            HttpStatusCode.TemporaryRedirect,
            HttpStatusCode.ServiceUnavailable
        ).forEach { code ->
            val response = responseWithStatus(code)
            assertFails { assert4xx(response) }
        }
    }

    @Test
    fun `5xx assertion works with 500-like responses`() {
        listOf(
            HttpStatusCode.InternalServerError,
            HttpStatusCode.GatewayTimeout,
            HttpStatusCode.InsufficientStorage
        ).forEach { code ->
            val response = responseWithStatus(code)
            assertDoesNotThrow { assert5xx(response) }
        }
    }

    @Test
    fun `5xx assertion does not work with other responses`() {
        listOf(
            HttpStatusCode.Continue,
            HttpStatusCode.Created,
            HttpStatusCode.TemporaryRedirect,
            HttpStatusCode.NotFound
        ).forEach { code ->
            val response = responseWithStatus(code)
            assertFails { assert5xx(response) }
        }
    }
}
