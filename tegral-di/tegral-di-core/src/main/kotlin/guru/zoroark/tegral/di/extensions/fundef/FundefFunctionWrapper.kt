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

package guru.zoroark.tegral.di.extensions.fundef

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.environment.EmptyQualifier
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.Injector
import guru.zoroark.tegral.di.environment.Qualifier
import guru.zoroark.tegral.di.environment.get
import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSupertypeOf

/**
 * Add a function definition to this environment for the given function.
 */
@ExperimentalFundef
@TegralDsl
fun <R> ContextBuilderDsl.putFundef(function: KFunction<R>) {
    put(ofFunction(function)) { FundefFunctionWrapper(scope, function, emptyMap()) }
}

/**
 * Add a function definition to this environment for the given function, with its configuration.
 */
@ExperimentalFundef
@TegralDsl
fun <R> ContextBuilderDsl.putFundef(configureDsl: FundefConfigureDsl<R>) {
    put(ofFunction(configureDsl.function), configureDsl.build())
}

/**
 * Retrieve the fundef wrapper for the given function, or throw an exception if no such fundef could be found.
 */
@ExperimentalFundef
fun <R> InjectionEnvironment.getFundefOf(function: KFunction<R>): FundefFunctionWrapper<R> {
    return get(ofFunction(function))
}

private sealed class Mapping<T> {
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

/**
 * Wrapper class for function definitions (fundefs).
 *
 * This class is responsible for turning regular function parameters into injections. You should not need to instantiate
 * this class yourself, consider using a variant of [putFundef] instead.
 */
@ExperimentalFundef
class FundefFunctionWrapper<R>(
    scope: InjectionScope,
    private val function: KFunction<R>,
    qualifiers: Map<String, Qualifier>
) {
    private val parameterMapping = function.parameters.associateWith { param ->
        val kclass = param.type.classifier?.classOrNull()
        if (kclass == null) {
            // TODO log here
            Mapping.Unknown
        } else {
            val qualifier = param.name?.let { name -> qualifiers[name] } ?: EmptyQualifier
            Mapping.Present(scope.optional(Identifier(kclass, qualifier)))
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
                    k.name != null && overrides.containsKey(k.name) ->
                        ParameterValue.FromOverride(overrides[k.name])

                    v is Mapping.Present<*> ->
                        ParameterValue.FromEnvironmentOrDefaultValue {
                            v.injector.getValue(this, ::parameterMapping)
                        }

                    k.isOptional ->
                        ParameterValue.FromDefaultValue

                    else ->
                        error(
                            "Missing value for parameter $k in function $function and the parameter is not optional."
                        )
                }
            }
            .toMutableMap()
        if (extension != Unit) {
            val extensionParam = function.extensionReceiverParameter
            requireNotNull(extensionParam) {
                "An extension extension was specified ($extension) but the function ($function) is not an extension " +
                    "function"
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

    /**
     * Invoke the wrapped function.
     *
     * You can provide overrides for parameters via [overrides], [instance] and [extension]. Otherwise:
     *
     * - for regular value parameters, a value with a corresponding type (and, if provided, qualifiers) will be injected
     * - for the instance or extension receiver parameters, this function will throw an exception.
     *
     * Returns the value the underlying function returns.
     *
     * If this function fails, you can use [checkCallable] with the same arguments to debug what may have gone wrong.
     */
    fun invoke(overrides: Map<String, Any?> = emptyMap(), instance: Any? = Unit, extension: Any? = Unit): R {
        val parameters = buildParametersMap(overrides, instance, extension)
        val actualParams = parameters
            .mapValues { it.value.actualValue }
            .filterValues { it is ParameterValue.ActualValue.Value }
            .mapValues { (it.value as ParameterValue.ActualValue.Value).value }
        return function.callBy(actualParams)
    }

    /**
     * This function checks the behavior [invoke] would have with the provided parameters. Refer to [invoke]'s
     * documentation for more details.
     */
    @Suppress("NestedBlockDepth")
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

                        is ParameterValue.FromDefaultValue ->
                            "its type could not be determined ahead of time. Provide a value via an override."

                        is ParameterValue.FromEnvironmentOrDefaultValue ->
                            "tried to inject it via a regular injection but it was not found in the injection " +
                                "environment. Either 'put' a compatible component or provide a value via an override."

                        is ParameterValue.FromOverride -> error("That should not happen")
                    }
                    errors += "${param.toHumanName()}: is not optional and does not have any value. Reason: $reason"
                }
            } else {
                if (actualValue is ParameterValue.ActualValue.Value && !isCompatible(param.type, actualValue.value)) {
                    errors += "${param.toHumanName()}: " +
                        "value ${actualValue.value} is not compatible with expected type ${param.type}"
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
