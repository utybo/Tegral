package guru.zoroark.tegral.di.test.check

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.EmptyQualifier
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectableModule
import guru.zoroark.tegral.di.environment.Qualifier
import kotlin.reflect.KClass

private const val NO_UNUSED_FOOTER_HELP =
    """
    If some or all of the components mentioned above are still used outside of injections (e.g. via a 'get' call on ^
    the environment), you can exclude them from this rule by adding them after the 'noUnused':
    
        +noUnused {
            exclude<ExcludeThis>()
            exclude<ExcludeThat>(named("exclude.that"))
            exclude(ExcludeIt::class)
            exclude(ExcludeMe::class, named("excluded"))
        }
    """

/**
 * Checks that all the components within modules are injected somewhere, except for the specified in the set passed as
 * a parameter.
 */
class NoUnusedCheck(private val ignoredValues: Set<Identifier<*>>) : TegralDiCheck {
    override fun check(modules: List<InjectableModule>) {
        val env = tegralDi(DependencyTrackingInjectionEnvironment) {
            modules.forEach { put(it) }
        }
        val dependencies = env.dependencies
        val usedInDependencies = dependencies.values.flatten().toSet()
        val deps = dependencies.keys.filter { it !in usedInDependencies && it !in ignoredValues }
        if (deps.isNotEmpty()) {
            val introLine =
                if (deps.size == 1) "The following component is not injected anywhere, making it unused."
                else "The following components are not injected anywhere, making them unused."
            val message = "'noUnused' check failed.\n" +
                "$introLine\n" +
                deps.joinToString(separator = "\n") { "--> $it" } + "\n\n" +
                NO_UNUSED_FOOTER_HELP.trimIndent().replace("^\n", "")
            throw TegralDiCheckException(message)
        }
    }
}

/**
 * Creates a [NoUnusedCheck].
 *
 * You can optionally add a lambda as a parameter to exclude specific components from this check.
 *
 * This check succeeding without any components being excluded means that [noCycle] will always fail.
 */
@TegralDsl
inline fun TegralDiCheckDsl.noUnused(block: NoUnusedCheckDsl.() -> Unit = {}) {
    checks.add(NoUnusedCheckDsl().apply(block).build())
}

/**
 * DSL receiver for creating a [NoUnusedCheck] with excluded components. Use the [exclude] functions to exclude
 * components by their identifiers.
 */
@TegralDsl
class NoUnusedCheckDsl {
    private val excludes = mutableSetOf<Identifier<*>>()

    /**
     * Excludes a given identifier from the unused check.
     */
    fun exclude(identifier: Identifier<*>) {
        excludes += identifier
    }

    /**
     * Builds a [NoUnusedCheck] based on the exclusions created via the [exclude] functions.
     */
    fun build(): NoUnusedCheck = NoUnusedCheck(excludes.toSet())
}

/**
 * Excludes an identifier built from the generic type parameter and the given qualifier, or [EmptyQualifier] if no
 * qualifier is provided.
 */
@TegralDsl
inline fun <reified T : Any> NoUnusedCheckDsl.exclude(qualifier: Qualifier = EmptyQualifier) {
    exclude(Identifier(T::class, qualifier))
}

/**
 * Excludes an identifier built from the given class and the given qualifier, or [EmptyQualifier] if no qualifier is
 * provided.
 */
@TegralDsl
fun NoUnusedCheckDsl.exclude(kclass: KClass<*>, qualifier: Qualifier = EmptyQualifier) {
    exclude(Identifier(kclass, qualifier))
}
