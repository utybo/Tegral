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

package guru.zoroark.tegral.di.test.check

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.InternalErrorException
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.EnvironmentContext
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionEnvironmentKind
import guru.zoroark.tegral.di.environment.Injector
import guru.zoroark.tegral.di.environment.ScopedContext
import guru.zoroark.tegral.di.environment.ScopedSupplierDeclaration
import guru.zoroark.tegral.di.test.NotAvailableInTestEnvironmentException
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KProperty

private class CrashOnUseEnvironment(context: EnvironmentContext) : InjectionEnvironment {
    override fun <T : Any> getOrNull(identifier: Identifier<T>): T? {
        throw NotAvailableInTestEnvironmentException(
            "getOrNull is not implemented and cannot be used during 'safeInjection' checks."
        )
    }

    companion object : InjectionEnvironmentKind<CrashOnUseEnvironment> {
        override fun build(context: EnvironmentContext): CrashOnUseEnvironment =
            CrashOnUseEnvironment(context)
    }

    private lateinit var currentInstantiation: Identifier<*>

    init {
        context.declarations.forEach { (i, v) ->
            currentInstantiation = i
            if (v is ScopedSupplierDeclaration) v.supplier(ScopedContext(EnvironmentBasedIgnoringMetaScope(this)))
        }
    }

    private fun generateErrorMessage(to: String): String =
        """
        'safeInjection' check failed.
        The following injection is done during the instantiation of $currentInstantiation:
            $currentInstantiation
        --> $to

        You *must not* actually perform injections during the instantiation of objects.
        If you need to do something on an object provided by an environment before storing it as a property, use ^
        'wrapIn' instead. See the documentation on the 'safeInjection' check for more details.
        """.trimIndent().replace("^\n", "")

    private inner class TrapInjector<T : Any>(private val target: Identifier<T>) : Injector<T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            throw TegralDiCheckException(generateErrorMessage(target.toString()))
        }
    }

    override fun <T : Any> createInjector(identifier: Identifier<T>, onInjection: (T) -> Unit): Injector<T> {
        return TrapInjector(identifier)
    }

    override fun getAllIdentifiers(): Sequence<Identifier<*>> {
        throw NotAvailableInTestEnvironmentException("getAllIdentifiers does not exist in CrashOnUseEnvironment")
    }
}

private object SafeInjectionCheck : TegralDiCheck {
    override fun check(modules: List<InjectableModule>) {
        try {
            tegralDi(CrashOnUseEnvironment) {
                modules.forEach { put(it) }
            }
        } catch (ex: InvocationTargetException) {
            val original = ex.getUpperCause<TegralDiCheckException>()
            throw original ?: throw InternalErrorException("Unexpected error in 'safeInjection' check", ex)
        }
    }
}

/**
 * Check that verifies no injection is actually performed during the instantiation of components.
 */
@TegralDsl
fun TegralDiCheckDsl.safeInjection() {
    checks.add(SafeInjectionCheck)
}

private inline fun <reified T : Throwable> InvocationTargetException.getUpperCause(): T? {
    var upper = cause
    while (upper != null && upper !is T) {
        upper = upper.cause
    }
    return if (upper == null) null else upper as T
}
