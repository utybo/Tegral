package guru.zoroark.tegral.openapi.dsl

import kotlin.test.Test
import kotlin.test.assertEquals

class PredefinedContentTypesTest : PredefinedContentTypesDsl {
    @Test
    fun `json content type`() {
        assertEquals("application/json", json.contentType)
    }

    @Test
    fun `xml content type`() {
        assertEquals("application/xml", xml.contentType)
    }

    @Test
    fun `plain text content type`() {
        assertEquals("text/plain", plainText.contentType)
    }

    @Test
    fun `form content type`() {
        assertEquals("application/x-www-form-urlencoded", form.contentType)
    }
}
