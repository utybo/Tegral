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

import guru.zoroark.tegral.core.TegralDsl

/**
 * Interface that contains standard content types.
 *
 * This interface is "implemented" (although nothing in it is abstract) by other DSL interfaces when they wish for users
 * to have directly access to utilities like `xml`, `json`...
 */
@TegralDsl
interface PredefinedContentTypesDsl {
    /**
     * [ContentType] object for `application/xml`
     */
    @TegralDsl
    val xml get() = ContentType("application/xml")

    /**
     * [ContentType] object for `application/json`
     */
    @TegralDsl
    val json get() = ContentType("application/json")

    /**
     * [ContentType] object for `application/x-www-form-urlencoded`
     */
    @TegralDsl
    val form get() = ContentType("application/x-www-form-urlencoded")

    /**
     * [ContentType] object for `text/plain`
     */
    @TegralDsl
    val plainText get() = ContentType("text/plain")
}
