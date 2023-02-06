package guru.zoroark.tegral.di.extensions.fundef

import kotlin.reflect.KParameter
import kotlin.reflect.KType

@ExperimentalFundef
data class FunctionSignature(
    val parameters: Map<String, ParameterSignature>,
    val returnType: KType,
    val extensionSignature: ParameterSignature? = null,
    val instanceSignature: ParameterSignature? = null
)

@ExperimentalFundef
data class ParameterSignature(val type: KType, val isOptional: Boolean = false)
