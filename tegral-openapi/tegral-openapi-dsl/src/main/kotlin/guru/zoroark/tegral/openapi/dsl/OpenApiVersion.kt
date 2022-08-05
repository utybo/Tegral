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
 * A version of the OpenAPI standard. This only includes the `x.y` part of the `x.y.z` version scheme.
 */
enum class OpenApiVersion(
    /**
     * The string that represents this version, in x.y format.
     */
    val version: String
) {
    /**
     * OpenAPI version 3.0.x
     */
    V3_0("3.0"),

    /**
     * OpenAPI version 3.1.x
     */
    V3_1("3.1")
}
