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

@JvmInline
value class ContentType(val contentType: String) {
    infix fun or(other: ContentType): MultiContentType = MultiContentType(listOf(this, other))
}

interface PredefinedContentTypesDsl {
    val xml get() = ContentType("application/xml")
    val json get() = ContentType("application/json")
    val form get() = ContentType("application/x-www-form-urlencoded")
    val plainText get() = ContentType("text/plain")
}
