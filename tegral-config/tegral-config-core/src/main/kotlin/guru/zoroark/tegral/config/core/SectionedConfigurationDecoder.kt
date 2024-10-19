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

package guru.zoroark.tegral.config.core

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import com.sksamuel.hoplite.transformer.PathNormalizer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

/**
 * Hoplite decoder for [SectionedConfiguration].
 *
 * Note that this decoder is not loaded automatically. You need to manually add it to the Hoplite builder with the
 * sections you are interested in.
 */
class SectionedConfigurationDecoder<T : SectionedConfiguration>(
    private val sectionedConfigurationType: KClass<T>,
    private val sectionedConfigurationTypeFactory: (Map<ConfigurationSection<*>, Any>) -> T,
    private val sections: List<ConfigurationSection<*>>
) : Decoder<T> {

    private fun createDefaultConfig(): ConfigResult<T> {
        val (optionalSections, requiredSections) = sections.partition {
            it.isOptional is SectionOptionality.Optional<*>
        }
        if (requiredSections.isNotEmpty()) {
            return ConfigFailure.Generic(
                "Missing required section(s): ${requiredSections.joinToString(", ") { it.name }}"
            ).invalid()
        } else {
            val sectionValues = optionalSections.associateWith {
                (it.isOptional as SectionOptionality.Optional<Any>).defaultValue
            }
            return sectionedConfigurationTypeFactory(sectionValues).valid()
        }
    }

    @Suppress("ReturnCount")
    override fun decode(node: Node, type: KType, context: DecoderContext): ConfigResult<T> {
        when (node) {
            is Undefined -> return createDefaultConfig()
            is NullNode -> return ConfigFailure.NullValueForNonNullField(node).invalid()
            !is MapNode -> return ConfigFailure.DecodeError(node, type).invalid()
            else -> {}
        }

        val sectionValues = mutableMapOf<ConfigurationSection<*>, Any>()
        for ((key, value) in node.map) {
            val section = sections.find { PathNormalizer.normalizePathElement(it.name) == key }
            val sectionType = section!!.kclass.starProjectedType

            val decodedSection = context.decoder(sectionType)
                .flatMap { decoder -> decoder.decode(value, sectionType, context) }

            if (decodedSection is Validated.Valid<Any?>) {
                context.usedPaths += value.path
                sectionValues[section] = decodedSection.value!!
            } else {
                return decodedSection as Validated.Invalid<ConfigFailure>
            }
        }

        val missingSections = sections.filter { !sectionValues.containsKey(it) }

        // Check if required sections are all present
        missingSections
            .filter { it.isOptional is SectionOptionality.Required }
            .map { it.name }
            .takeIf { it.isNotEmpty() }
            ?.let {
                return ConfigFailure.Generic(
                    "Missing required sections: ${it.joinToString(", ")}"
                ).invalid()
            }

        // Fill in any absent optional section
        missingSections.filter { it.isOptional is SectionOptionality.Optional }
            .forEach {
                sectionValues[it] = (it.isOptional as SectionOptionality.Optional).defaultValue
            }

        val sectionedConfig = sectionedConfigurationTypeFactory(sectionValues)
        return sectionedConfig.valid()
    }

    override fun supports(type: KType): Boolean {
        return type.isSubtypeOf(sectionedConfigurationType.starProjectedType)
    }
}
