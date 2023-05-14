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

import guru.zoroark.tegral.prismakt.generator.parser.NiwenPrism
import guru.zoroark.tegral.prismakt.generator.parser.PRoot
import guru.zoroark.tegral.prismakt.generator.protocol.Datamodel
import java.nio.file.Path

/**
 * Context for [generators][ModelGenerator]
 */
class GeneratorContext(
    /**
     * Output directory where classes, etc. should be placed.
     */
    val outputDir: Path,
    /**
     * Prisma's DMMF Datamodel.
     */
    val datamodel: Datamodel,
    /**
     * Raw [NiwenPrism] result, or null if the Niwen Prism parsing failed.
     */
    val rawParsingResult: PRoot?
)
