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

package guru.zoroark.tegral.web.controllers

import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.MultiQualifier
import guru.zoroark.tegral.di.environment.Qualifier
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.extensions.DeclarationsProcessor
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import guru.zoroark.tegral.di.extensions.fundef.ExperimentalFundef
import guru.zoroark.tegral.di.extensions.fundef.FunctionQualifier
import guru.zoroark.tegral.di.extensions.fundef.FundefFunctionWrapper
import io.ktor.server.application.Application
import io.ktor.server.routing.Routing
import kotlin.reflect.KClass
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.typeOf

/**
 * The Ktor extension object that is injected into the meta-environment. Keeps track of implementations of
 * [KtorModule] subclasses (incl. [KtorController] subclasses) within the main environment.
 */
class KtorExtension(scope: InjectionScope, private val enableFundefs: Boolean = false) : DeclarationsProcessor {
    private val environment: ExtensibleInjectionEnvironment by scope()

    private val modulesIdentifiers = mutableListOf<Identifier<out KtorModule>>()
    private val fundefs = mutableListOf<ResolvableFundef>()

    /**
     * Returns modules available in the environment for the given application name.
     */
    fun getModulesForAppName(appName: String?): List<KtorModule> {
        val modules = fundefs.map { it.resolveFrom(environment) }
        return environment.getKtorModulesByPriority(modulesIdentifiers, AppNameConstraint.App(appName), modules)
    }

    /**
     * Returns all available modules, including modules for any app.
     *
     * Consider using [getModulesForAppName] instead.
     */
    fun getAllModules(): List<KtorModule> {
        val modules = fundefs.map { it.resolveFrom(environment) }
        return environment.getKtorModulesByPriority(modulesIdentifiers, AppNameConstraint.Any, modules)
    }

    @OptIn(ExperimentalFundef::class)
    override fun processDeclarations(sequence: Sequence<Declaration<*>>) {
        modulesIdentifiers += sequence.map { it.identifier }.filterIsKclassSubclassOf()
        sequence
            .takeIf { enableFundefs }
            ?.map { it.identifier }
            ?.filterIsKclassSubclassOf<FundefFunctionWrapper<*>>()
            ?.mapNotNull {
                val extParameter =
                    it.qualifier.findQualifier(FunctionQualifier::class)?.function?.extensionReceiverParameter
                when (extParameter?.type) {
                    typeOf<Routing>() -> ResolvableFundef(it, ResolvableFundefType.CONTROLLER)
                    typeOf<Application>() -> ResolvableFundef(it, ResolvableFundefType.MODULE)
                    else -> null
                }
            }
            ?.let { fundefs += it }
    }

    @OptIn(ExperimentalFundef::class)
    private class ResolvableFundef(
        val identifier: Identifier<out FundefFunctionWrapper<*>>,
        private val fundefType: ResolvableFundefType
    ) {
        fun resolveFrom(environment: InjectionEnvironment): KtorModule {
            return toModule(environment.get(identifier))
        }

        private fun toModule(fundef: FundefFunctionWrapper<*>): KtorModule {
            return when (fundefType) {
                ResolvableFundefType.MODULE -> FundefBackedModule(fundef)
                ResolvableFundefType.CONTROLLER -> FundefBackedController(fundef)
            }
        }
    }

    private enum class ResolvableFundefType {
        MODULE,
        CONTROLLER
    }

    @OptIn(ExperimentalFundef::class)
    private class FundefBackedModule(private val fundef: FundefFunctionWrapper<*>) : KtorModule() {
        override fun Application.install() {
            fundef.invoke(extension = this)
        }
    }

    @OptIn(ExperimentalFundef::class)
    private class FundefBackedController(private val fundef: FundefFunctionWrapper<*>) : KtorController() {
        override fun Routing.install() {
            fundef.invoke(extension = this)
        }
    }
}

private fun <T : Qualifier> Qualifier.findQualifier(kclass: KClass<T>): T? =
    when {
        kclass.isInstance(this) -> this as T
        this is MultiQualifier -> this.qualifiers.firstNotNullOf { it.findQualifier(kclass) }
        else -> null
    }

/**
 * Utility functions that filters declarations to only those that are subclasses of [KtorModule], and returns a properly
 * typed list for them.
 */
inline fun <reified T : Any> Sequence<Identifier<*>>.filterIsKclassSubclassOf(): List<Identifier<out T>> {
    return filter { it.kclass.isSubclassOf(T::class) }
        .filterIsInstance<Identifier<out T>>() // Note that this does not actually do anything due to type erasure
        .toList()
}

/**
 * A constraint for retrieving modules related to some application in [getKtorModulesByPriority].
 */
sealed class AppNameConstraint {
    /**
     * Returns true if the provided module corresponds to this constraint.
     */
    abstract fun acceptsModule(module: KtorModule): Boolean

    /**
     * Constraint to retrieve modules with applications that have a specific name. Note that 'null' is a valid
     * application name: it is the default application's name.
     *
     * If you want to get *all* modules regardless of their assigned application, use [AppNameConstraint.Any] instead.
     *
     * @property appName Name of the app
     */
    class App(val appName: String?) : AppNameConstraint() {
        override fun acceptsModule(module: KtorModule): Boolean =
            module.restrictToAppName == appName
    }

    /**
     * Get any module, regardless of their assigned name.
     */
    object Any : AppNameConstraint() {
        override fun acceptsModule(module: KtorModule) =
            true
    }
}

/**
 * Retrieves all implementations of [KtorModule] subclasses in the given environment, sorted by decreasing priority.
 */
@Deprecated("Use AppNameConstraint.App instead of a String for the appName parameter")
fun InjectionEnvironment.getKtorModulesByPriority(
    allIdentifiers: List<Identifier<out KtorModule>>,
    appName: String?,
    additionalModules: List<KtorModule>
): List<KtorModule> = getKtorModulesByPriority(allIdentifiers, AppNameConstraint.App(appName), additionalModules)

/**
 * Retrieves all implementations of [KtorModule] subclasses in the given environment, sorted by decreasing priority.
 */
fun InjectionEnvironment.getKtorModulesByPriority(
    allIdentifiers: List<Identifier<out KtorModule>>,
    appNameConstraint: AppNameConstraint,
    additionalModules: List<KtorModule>
): List<KtorModule> {
    return allIdentifiers.asSequence()
        .map { get(it) }
        .plus(additionalModules)
        .filter { appNameConstraint.acceptsModule(it) }
        .sortedByDescending { it.moduleInstallationPriority }
        .toList()
}
