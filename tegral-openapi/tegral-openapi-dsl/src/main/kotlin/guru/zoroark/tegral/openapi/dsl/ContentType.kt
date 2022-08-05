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

/**
 * A content type.
 *
 * This is not meant to be a full representation, but a handy shortcut for defining media types in various DSLs.
 *
 * Pre-defined values for very common content types are available in [PredefinedContentTypesDsl].
 */
@JvmInline
value class ContentType(
    /**
     * The actual value of the content type (in full string representation).
     */
    val contentType: String
) {
    /**
     * Combines this content type with another to form a [MultiContentType].
     */
    infix fun or(other: ContentType): MultiContentType = MultiContentType(listOf(this, other))
}
