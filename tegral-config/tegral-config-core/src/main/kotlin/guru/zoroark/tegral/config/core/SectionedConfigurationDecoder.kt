package guru.zoroark.tegral.config.core

import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.MapNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.decoder.NullHandlingDecoder
import com.sksamuel.hoplite.fp.Validated
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.typeOf

class SectionedConfigurationDecoder(
    private val sections: List<ConfigurationSection<*>>
) : NullHandlingDecoder<SectionedConfiguration> {
    @Suppress("ReturnCount")
    override fun safeDecode(node: Node, type: KType, context: DecoderContext): ConfigResult<SectionedConfiguration> {
        if (node !is MapNode) {
            return ConfigFailure.DecodeError(node, type).invalid()
        }
        val sectionValues = mutableMapOf<ConfigurationSection<*>, Any>()
        for ((key, value) in node.map) {
            val section = sections.find { it.name == key }
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

        val sectionedConfig = SectionedConfiguration(sectionValues)
        return sectionedConfig.valid()
    }

    override fun supports(type: KType): Boolean {
        return type == typeOf<SectionedConfiguration>()
    }
}
