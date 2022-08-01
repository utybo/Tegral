package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.tags.Tag
import kotlin.test.assertEquals
import kotlin.test.Test

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
