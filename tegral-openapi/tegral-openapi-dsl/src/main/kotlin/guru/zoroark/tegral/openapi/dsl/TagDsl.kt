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

/**
 * DSL for the [tag object](https://spec.openapis.org/oas/v3.1.0#tag-object).
 *
 * Note that the `externalDocs` field is embedded in this DSL.
 *
 * The name of the tag is passed as a parameter to the builder.
 */
interface TagDsl {
    /**
     * A description for the tag. CommonMark syntax may be used for rich text representation.
     */
    var description: String?

    /**
     * Description for the external documentation for this tag.
     */
    var externalDocsDescription: String?

    /**
     * The URL for the external documentation for this tag. Must be in the form of a URL.
     */
    var externalDocsUrl: String?
}

/**
 * Actual builder for [TagDsl].
 *
 * @param name The name of the tag. This is the same value that is put in the [tags][OperationDsl.tags] property of
 * operations.
 */
class TagBuilder(private val name: String) : TagDsl, Builder<Tag> {
    override var description: String? = null
    override var externalDocsDescription: String? = null
    override var externalDocsUrl: String? = null

    override fun build(): Tag = Tag().apply {
        name = this@TagBuilder.name
        description = this@TagBuilder.description
        if (externalDocsDescription != null || externalDocsUrl != null) {
            externalDocs = ExternalDocumentation().apply {
                description = externalDocsDescription
                url = externalDocsUrl
            }
        }
    }
}
