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

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.tags.Tag
import kotlin.test.Test
import kotlin.test.assertEquals

class TagBuilderTest {
    @Test
    fun `Full test`() {
        val tag = TagBuilder("tag-name").apply {
            description = "This is a tag"
            externalDocsDescription = "This is an external docs"
            externalDocsUrl = "https://example.com"
        }

        val expected = Tag().apply {
            name = "tag-name"
            description = "This is a tag"
            externalDocs = ExternalDocumentation().apply {
                description = "This is an external docs"
                url = "https://example.com"
            }
        }

        assertEquals(expected, tag.build())
    }

    @Test
    fun `Test missing external docs url`() {
        val tag = TagBuilder("tag-name").apply {
            description = "This is a tag"
            externalDocsDescription = "This is an external docs"
        }

        val expected = Tag().apply {
            name = "tag-name"
            description = "This is a tag"
            externalDocs = ExternalDocumentation().apply {
                description = "This is an external docs"
            }
        }

        assertEquals(expected, tag.build())
    }

    @Test
    fun `Test missing external docs description`() {
        val tag = TagBuilder("tag-name").apply {
            description = "This is a tag"
            externalDocsUrl = "https://example.com"
        }

        val expected = Tag().apply {
            name = "tag-name"
            description = "This is a tag"
            externalDocs = ExternalDocumentation().apply {
                url = "https://example.com"
            }
        }

        assertEquals(expected, tag.build())
    }

    @Test
    fun `Test missing all external docs fields`() {
        val tag = TagBuilder("tag-name").apply {
            description = "This is a tag"
        }

        val expected = Tag().apply {
            name = "tag-name"
            description = "This is a tag"
        }

        assertEquals(expected, tag.build())
    }
}
