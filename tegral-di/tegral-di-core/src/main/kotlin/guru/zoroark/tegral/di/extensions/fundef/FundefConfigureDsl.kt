package guru.zoroark.tegral.di.extensions.fundef

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.environment.Qualifier
import guru.zoroark.tegral.di.environment.ScopedContext
import kotlin.reflect.KFunction

@ExperimentalFundef
class FundefConfigureDsl<R>(val function: KFunction<R>) {
    val qualifiers = mutableMapOf<String, Qualifier>()

    @TegralDsl
    infix fun String.qualifyWith(qualifier: Qualifier) {
        qualifiers[this] = qualifier
    }

    fun build(): ScopedContext.() -> FundefFunctionWrapper<R> {
        return {
            FundefFunctionWrapper(scope, function, qualifiers)
        }
    }
}

@ExperimentalFundef
fun <R> KFunction<R>.configureFundef(block: FundefConfigureDsl<R>.() -> Unit): FundefConfigureDsl<R> {
    return FundefConfigureDsl(this).apply(block)
}