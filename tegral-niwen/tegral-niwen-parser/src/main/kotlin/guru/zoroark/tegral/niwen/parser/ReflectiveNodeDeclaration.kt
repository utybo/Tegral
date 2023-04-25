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

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmName

/**
 * Creates a node declaration that uses reflection to initialize instances of
 * the nodes.
 *
 * Intended to be used with delegated implementations:
 *
 *  ```
 *  data class MyNode(...) {
 *      companion object : ParserNodeDeclaration<MyNode> by reflective()
 *  }
 *  ```
 *
 *  @see ReflectiveNodeDeclaration
 */
@TegralDsl
inline fun <reified T : Any> reflective(): ParserNodeDeclaration<T> = ReflectiveNodeDeclaration(T::class)

/**
 * An implementation of [ParserNodeDeclaration] that uses reflection to
 * initialize instances.
 *
 * The arguments of the node class' constructor are filled in using the
 * arguments of the [TypeDescription] passed to the [make] function.
 * Keys are mapped to the constructors' argument parameter names, and the first
 * match is used to initialize the class.
 *
 * This class supports classes with multiple constructors: just remember that
 * constructors are chosen exclusively based on parameter names, not typing.
 * TODO is that still true?
 */
class ReflectiveNodeDeclaration<T : Any>(
    private val tClass: KClass<T>
) : ParserNodeDeclaration<T> {
    private fun KParameter.toKey(): NodeParameterKey<T, *>? {
        return this.name?.let { name -> NodeParameterKey<T, Nothing>(this.type, name) }
    }

    override fun make(args: TypeDescription<T>): T {
        val ctor = findValidConstructor(args.arguments)
        // TODO Better error messages on reflection errors
        val callArgs = ctor.parameters
            .associateWith {
                it.toKey() ?: throw NiwenParserException(
                    "Internal error: a name-less parameter was returned by findValidConstructor"
                )
            }
            .filterNot { (param, key) ->
                param.isOptional && !args.arguments.containsKey(key)
            }
            .mapValues { (_, key) ->
                args.arguments.getOrElse(key) {
                    throw NiwenParserException(
                        "Internal error: could not find a parameter despite the constructor passing " +
                            "findValidConstructor"
                    )
                }
            }

        return ctor.callBy(callArgs)
    }

    private sealed class ConstructorResult<T> {
        class Valid<T>(val ctor: KFunction<T>) : ConstructorResult<T>()
        class Invalid<T>(val ctor: KFunction<T>, val param: KParameter, val reason: String) : ConstructorResult<T>()
    }

    private fun findValidConstructor(arguments: Map<NodeParameterKey<T, *>, *>): KFunction<T> {
        val constructorsMatched = tClass.constructors.map { constructor ->
            // All parameters have a value in the map (except for optional parameters)
            // with a compatible type
            val firstInvalid: ConstructorResult.Invalid<T>? = constructor.parameters.firstNotNullOfOrNull { ctorParam ->
                val ctorParamName = ctorParam.name
                    ?: return@firstNotNullOfOrNull ConstructorResult.Invalid(
                        constructor,
                        ctorParam,
                        "Parameter does not have a name"
                    )
                val matchingArg = arguments[NodeParameterKey<T, Nothing>(ctorParam.type, ctorParamName)]
                if (matchingArg == null) {
                    if (ctorParam.isOptional) {
                        null
                    } else {
                        ConstructorResult.Invalid(
                            constructor,
                            ctorParam,
                            "No available value and the parameter is not optional"
                        )
                    }
                } else {
                    null
                }
            }

            firstInvalid ?: ConstructorResult.Valid(constructor)
        }

        return constructorsMatched
            .filterIsInstance<ConstructorResult.Valid<T>>()
            .maxByOrNull {
                // Pick the constructor with the most non-optional parameters
                it.ctor.parameters.count { param -> !param.isOptional }
            }
            ?.ctor
            ?: throw NiwenParserException(createNoValidConstructorMessage(constructorsMatched, arguments))
    }

    private fun createNoValidConstructorMessage(
        constructorsMatched: List<ConstructorResult<T>>,
        arguments: Map<NodeParameterKey<T, *>, *>
    ): String {
        return buildString {
            appendLine("Could not find a constructor that uses parameters")
            appendLine(arguments.entries.joinToString("\n") { (k, _) -> "- ${k.name}: ${k.outputType}" })
            appendLine()
            appendLine("None of the following constructors matched:")
            constructorsMatched
                .filterIsInstance<ConstructorResult.Invalid<T>>()
                .forEach { match ->
                    append("Constructor ${match.ctor.returnType}(")
                    append(match.ctor.parameters.joinToString(", ") { "${it.name}: ${it.type}" })
                    appendLine(")")
                    appendLine("  -> ${match.reason}")
                }
            appendLine()
            appendLine("Available parameters:")
            appendLine(
                arguments.entries.joinToString("\n") { (k, v) ->
                    "  -> ${k.name}: ${k.outputType} = $v"
                }
            )
        }
    }

    override val nodeName: String = tClass.simpleName ?: tClass.jvmName
}
