package guru.zoroark.tegral.web.controllers.test

import io.ktor.client.statement.HttpResponse
import kotlin.test.assertTrue

/**
 * Asserts that the response's status code is in the 2xx range (OK).
 */
fun assert2xx(response: HttpResponse) {
    assertTrue(response.status.value in 200..299, "Expected 2xx response, got ${response.status.value}")
}

/**
 * Asserts that the response's status code is in the 3xx range (Redirect).
 */
fun assert3xx(response: HttpResponse) {
    assertTrue(response.status.value in 300..399, "Expected 3xx response, got ${response.status.value}")
}

/**
 * Asserts that the response's status code is in the 4xx range (client error).
 */
fun assert4xx(response: HttpResponse) {
    assertTrue(response.status.value in 400..499, "Expected 4xx response, got ${response.status.value}")
}

/**
 * Asserts that the response's status code is in the 5xx range (server error).
 */
fun assert5xx(response: HttpResponse) {
    assertTrue(response.status.value in 500..599, "Expected 5xx response, got ${response.status.value}")
}
