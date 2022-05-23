package guru.zoroark.tegral.web.controllers

import guru.zoroark.tegral.di.environment.*
import guru.zoroark.tegral.di.extensions.DeclarationsProcessor
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import kotlin.reflect.full.isSubclassOf


/**
 * The Ktor extension object that is injected into the meta-environment. Keeps track of implementations of
 * [KtorModule] subclasses (incl. [KtorController] subclasses) within the main environment.
 */
class KtorExtension(scope: InjectionScope) : DeclarationsProcessor {
    private val environment: ExtensibleInjectionEnvironment by scope()

    /**
     * Returns modules available in the environment for the given application name.
     */
    fun getModulesForAppName(appName: String?): List<KtorModule> {
        return environment.getKtorModulesByPriority(modulesIdentifiers, appName)
    }

    private val modulesIdentifiers = mutableListOf<Identifier<out KtorModule>>()

    override fun processDeclarations(sequence: Sequence<Declaration<*>>) {
        modulesIdentifiers += sequence.map { it.identifier}.filterIsKclassSubclassOf()
    }
}

inline fun <reified T : Any> Sequence<Identifier<*>>.filterIsKclassSubclassOf(): List<Identifier<out T>> {
    return filter { it.kclass.isSubclassOf(T::class) }
            .filterIsInstance<Identifier<out T>>() // Note that this does not actually do anything due to type erasure
            .toList()
}

fun InjectionEnvironment.getKtorModulesByPriority(allIdentifiers: List<Identifier<out KtorModule>>, appName: String?): List<KtorModule> {
    return allIdentifiers
            .map { get(it) }
            .filter { it.restrictToAppName == appName }
            .sortedByDescending { it.moduleInstallationPriority }
}
