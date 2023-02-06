package guru.zoroark.tegral.di.extensions.fundef

import guru.zoroark.tegral.di.environment.Qualifier
import kotlin.reflect.KFunction

data class FunctionQualifier<R>(val function: KFunction<R>) : Qualifier

fun <R> ofFunction(function: KFunction<R>) : Qualifier = FunctionQualifier(function)