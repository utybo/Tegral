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
