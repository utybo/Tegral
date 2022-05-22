package guru.zoroark.tegral.web.controllers

import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.invoke
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
        return modulesIdentifiers
            .map { environment.get(it) }
            .filterIsInstance<KtorModule>()
            .filter { it.restrictToAppName == appName }
            .sortedByDescending { it.moduleInstallationPriority }
    }

    private val modulesIdentifiers = mutableListOf<Identifier<*>>()

    override fun processDeclarations(sequence: Sequence<Declaration<*>>) {
        sequence
            .filter { it.identifier.kclass.isSubclassOf(KtorModule::class) }
            .forEach { modulesIdentifiers += it.identifier }
    }
}
