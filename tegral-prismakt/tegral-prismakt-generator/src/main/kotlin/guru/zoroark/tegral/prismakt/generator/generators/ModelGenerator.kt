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

package guru.zoroark.tegral.prismakt.generator.generators

/**
 * A generator that takes in a [context][GeneratorContext] and does *something* with it.
 *
 * Most implementations generate Kotlin classes with KotlinPoet using the information contained in the
 * [GeneratorContext].
 */
interface ModelGenerator {
    /**
     * Perform the model generation with the provided [context].
     */
    fun generateModels(context: GeneratorContext)
}
