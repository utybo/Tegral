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

package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.parser.expectations.Expectation

/**
 * A node type declaration (providing a way to make the actual type) with its
 * descriptor (which declares what the type expects).
 *
 * @property type The type described
 *
 * @property expectations The descriptor: currently, just a list of the
 * expectations that make up this type
 */
class DescribedType<T>(
    val type: ParserNodeDeclaration<T>,
    val expectations: List<Expectation<T, *>>
)
