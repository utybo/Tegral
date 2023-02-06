package guru.zoroark.tegral.di.extensions.fundef

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.environment.*
import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSupertypeOf

@ExperimentalFundef()
fun <R> signatureOf(function: KFunction<R>): FunctionSignature {
    val regularParameters = function.parameters.filter { it.kind == KParameter.Kind.VALUE }
        .associateBy({
            it.name
                ?: error("In function ${function.name}, a regular parameter does not have a name. Please report this to the Tegral maintainers.")
        }) { ParameterSignature(it.type, it.isOptional) }
    val extensionReceiverParam = function.parameters.find { it.kind == KParameter.Kind.EXTENSION_RECEIVER }
    val extensionReceiverSignature = extensionReceiverParam?.let { ParameterSignature(it.type, it.isOptional) }
    val instanceReceiverParam = function.parameters.find { it.kind == KParameter.Kind.INSTANCE }
    val instanceReceiverSignature = instanceReceiverParam?.let { ParameterSignature(it.type, it.isOptional) }
    return FunctionSignature(
        regularParameters,
        function.returnType,
        extensionReceiverSignature,
        instanceReceiverSignature
    )
}

@ExperimentalFundef
@TegralDsl
fun <R> ContextBuilderDsl.putFundef(function: KFunction<R>) {
    put(ofFunction(function)) { FundefFunctionWrapper(scope, function) }
}

@ExperimentalFundef
fun <R> InjectionEnvironment.getFundefOf(function: KFunction<R>): FundefFunctionWrapper<R> {
    return get(ofFunction(function))
}

sealed class Mapping<T> {
    object Unknown : Mapping<Nothing>()
    class Present<T>(val injector: Injector<T>) : Mapping<T>()
}

private sealed class ParameterValue {
    abstract val actualValue: ActualValue

    sealed class ActualValue {
        object Undefined : ActualValue()
        class Value(val value: Any?) : ActualValue()
    }

    object FromDefaultValue : ParameterValue() {
        override val actualValue = ActualValue.Undefined
    }

    class FromOverride(val value: Any?) : ParameterValue() {
        override val actualValue = ActualValue.Value(value)
    }

    class FromEnvironmentOrDefaultValue(val provider: () -> Any?) : ParameterValue() {
        override val actualValue: ActualValue
            get() = provider()?.let { ActualValue.Value(it) } ?: ActualValue.Undefined
    }
}

@ExperimentalFundef
class FundefFunctionWrapper<R>(scope: InjectionScope, private val function: KFunction<R>) {
    val signature = signatureOf(function)
    private val parameterMapping = function.parameters.associateWith {
        val kclass = it.type.classifier?.classOrNull()
        if (kclass == null) {
            // TODO log here
            Mapping.Unknown
        } else {
            Mapping.Present(scope.optional(Identifier(kclass)))
        }
    }

    init {
        require(function.typeParameters.isEmpty()) { "Functions with type parameters are not supported." }
    }

    private fun buildParametersMap(
        overrides: Map<String, Any?>,
        instance: Any?,
        extension: Any?
    ): Map<KParameter, ParameterValue> {
        val parameters = parameterMapping
            .mapValues { (k, v) ->
                when {
                    k.name != null && overrides.containsKey(k.name) -> ParameterValue.FromOverride(overrides[k.name])
                    v is Mapping.Present<*> ->
                        ParameterValue.FromEnvironmentOrDefaultValue { v.injector.getValue(this, ::parameterMapping) }

                    k.isOptional -> ParameterValue.FromDefaultValue
                    else -> error("Missing value for parameter $k in function $function and the parameter is not optional.")
                }
            }
            .toMutableMap()
        if (extension != Unit) {
            val extensionParam = function.extensionReceiverParameter
            requireNotNull(extensionParam) {
                "An extension extension was specified ($extension) but the function ($function) is not an extension function "
            }
            parameters[extensionParam] = ParameterValue.FromOverride(extension)
        }
        if (instance != Unit) {
            val instanceParam = function.instanceParameter
            requireNotNull(instanceParam) {
                "An instance was specified ($instance) but the function ($function) is not an instance function)"
            }
            parameters[instanceParam] = ParameterValue.FromOverride(instance)
        }
        return parameters
    }

    fun invoke(overrides: Map<String, Any?> = emptyMap(), instance: Any? = Unit, extension: Any? = Unit): R {
        val parameters = buildParametersMap(overrides, instance, extension)
        val actualParams = parameters
            .mapValues { it.value.actualValue }
            .filterValues { it is ParameterValue.ActualValue.Value }
            .mapValues { (it.value as ParameterValue.ActualValue.Value).value }
        return function.callBy(actualParams)
    }

    fun checkCallable(overrides: Map<String, Any?> = emptyMap(), instance: Any? = Unit, extension: Any? = Unit) {
        val errors = mutableListOf<String>()
        val parameters = buildParametersMap(overrides, instance, extension)
        for (param in function.parameters) {
            val suppliedParam = parameters[param]
            val actualValue = suppliedParam?.actualValue
            if (suppliedParam == null || actualValue is ParameterValue.ActualValue.Undefined) {
                if (!param.isOptional) {
                    val reason = when (suppliedParam) {
                        null ->
                            "it is a special parameter that is not determined automatically. Provide its value " +
                                "manually via the 'instance' or 'extension' parameters."

                        is ParameterValue.FromDefaultValue -> "its type could not be determined ahead of time. " +
                            "Provide a value via an override."

                        is ParameterValue.FromEnvironmentOrDefaultValue -> "tried to inject it via a regular " +
                            "injection but it was not found in the injection environment. Either 'put' a compatible " +
                            "component or provide a value via an override."

                        is ParameterValue.FromOverride -> error("That should not happen")
                    }
                    errors += "${param.toHumanName()}: is not optional and does not have any value. Reason: $reason"
                }
            } else {
                if (actualValue is ParameterValue.ActualValue.Value && !isCompatible(param.type, actualValue.value)) {
                    errors += "${param.toHumanName()}: value ${actualValue.value} is not compatible with expected type ${param.type}"
                }
            }
        }
        if (errors.isNotEmpty()) {
            error("Call with provided arguments will fail:\n" + errors.joinToString(separator = "\n"))
        }
    }
}

private fun KClassifier.classOrNull(): KClass<*>? {
    return this as? KClass<*>
}

private fun KParameter.toHumanName() =
    name ?: when (kind) {
        KParameter.Kind.INSTANCE -> "<instance parameter>"
        KParameter.Kind.EXTENSION_RECEIVER -> "<extension receiver>"
        KParameter.Kind.VALUE -> "<unnamed value param.>"
    }

private fun isCompatible(type: KType, value: Any?) =
    if (value == null) {
        type.isMarkedNullable
    } else {
        type.isSupertypeOf(value::class.createType())
    }
