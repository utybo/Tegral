package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import javax.swing.text.html.parser.Parser
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

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
                it.toKey() ?: throw NiwenParserException("Internal error: a name-less parameter was returned by findValidConstructor")
            }
            .filterNot { (param, key) ->
                param.isOptional && !args.arguments.containsKey(key)
            }
            .mapValues { (_, key) ->
                args.arguments.getOrElse(key) {
                    throw NiwenParserException("Internal error: could not find a parameter despite the constructor passing findValidConstructor")
                }
            }

        return ctor.callBy(callArgs)
    }

    private fun findValidConstructor(arguments: Map<NodeParameterKey<T, *>, *>): KFunction<T> {
        return tClass.constructors.filter {
            // All parameters have a value in the map (except for optional parameters)
            // with a compatible type
            it.parameters.all { param ->
                val paramName = param.name ?: return@all false
                val matchingArg = arguments[NodeParameterKey<T, Nothing>(param.type, paramName)]
                    ?: return@all param.isOptional
                param.isCompatibleWith(matchingArg)
            }
        }.maxByOrNull {
            // Pick the constructor with the most non-optional parameters
            it.parameters.count { param -> !param.isOptional }
        } ?: throw NiwenParserException(
            "Could not find a constructor that uses keys " +
                arguments.entries.joinToString(", ") { (k, v) -> k.toString() }
        )
    }
}

private fun KParameter.isCompatibleWith(matchingArg: Any): Boolean =
    matchingArg::class.starProjectedType.isSubtypeOf(type)