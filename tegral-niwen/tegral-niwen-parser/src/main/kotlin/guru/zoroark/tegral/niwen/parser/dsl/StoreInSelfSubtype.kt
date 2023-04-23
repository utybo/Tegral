package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.TypeDescription
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmName
import kotlin.reflect.typeOf


fun <T, R : T> selfKeyFor(type: KType) = NodeParameterKey<T, R>(type, "self")

@TegralDsl
inline fun <reified T, R : T> ExpectationReceiver<T>.self(): NodeParameterKey<T, R> {
    return selfKeyFor(typeOf<T>())
}

@TegralDsl
inline fun <reified T> subtype(): ParserNodeDeclaration<T> {
    val type = typeOf<T>()
    return object : ParserNodeDeclaration<T> {
        override fun make(args: TypeDescription<T>): T {
            return args[selfKeyFor(type)]
        }

        override val nodeName = T::class.simpleName ?: T::class.jvmName
    }
}
