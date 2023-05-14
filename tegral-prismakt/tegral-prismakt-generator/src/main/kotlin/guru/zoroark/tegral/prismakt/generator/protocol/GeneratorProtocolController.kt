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

package guru.zoroark.tegral.prismakt.generator.protocol

import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.prismakt.generator.ExecutionContext
import guru.zoroark.tegral.prismakt.generator.generators.ExposedDaoGenerator
import guru.zoroark.tegral.prismakt.generator.generators.ExposedSqlGenerator
import guru.zoroark.tegral.prismakt.generator.generators.GeneratorContext
import guru.zoroark.tegral.prismakt.generator.genericLogger
import guru.zoroark.tegral.prismakt.generator.parser.NiwenPrismParser
import java.nio.file.Path

private const val DEFAULT_OUTPUT_DIR = "generatedSrc"

/**
 * A simplified implementation of [GeneratorProtocolHandler].
 */
class GeneratorProtocolController(scope: InjectionScope) : GeneratorProtocolHandler {
    private val exposedSqlGenerator: ExposedSqlGenerator by scope()
    private val exposedDaoGenerator: ExposedDaoGenerator by scope()
    private val parser: NiwenPrismParser by scope()
    private val executionContext: ExecutionContext by scope()

    override fun getManifest(request: GeneratorRequest.GetManifestRequest): GeneratorResponse.GetManifestResponse {
        return GeneratorResponse.GetManifestResponse(
            id = request.id,
            jsonrpc = "2.0",
            result = GeneratorResponse.GetManifestResponse.GeneratorManifestContainer(
                GeneratorManifest(
                    prettyName = "Tegral PrismaKt Generator",
                    defaultOutput = DEFAULT_OUTPUT_DIR
                )
            )
        )
    }

    override fun generate(request: GeneratorRequest.GenerateRequest) {
        genericLogger.info(
            "Will generate bindings for the following table(s): " +
                request.params.dmmf.datamodel.models.joinToString { it.name }
        )
        genericLogger.debug(request.params.dmmf.datamodel.toString())
        val outputDir = request.params.generator.output?.value ?: DEFAULT_OUTPUT_DIR
        val generationContext = GeneratorContext(
            Path.of(outputDir),
            request.params.dmmf.datamodel,
            parser.parseOrNull(Path.of(request.params.schemaPath), executionContext.enableParsingDebug)
        )
        val target = request.params.generator.config["exposedTarget"]
            ?.also { genericLogger.debug("Using exposedTarget: $it") }
            ?: "dao".also { genericLogger.debug("No exposedTarget defined: using 'dao' target") }
        when (target.lowercase()) {
            "sql" -> exposedSqlGenerator.generateModels(generationContext)
            "dao" -> exposedDaoGenerator.generateModels(generationContext)
            else -> error("Unknown exposedTarget: \"$target\"")
        }
        genericLogger.info("Done!")
    }
}
