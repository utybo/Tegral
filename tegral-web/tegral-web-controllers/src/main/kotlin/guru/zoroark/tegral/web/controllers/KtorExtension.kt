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
        return environment.getKtorModulesByPriority(modulesIdentifiers, appName)
    }

    private val modulesIdentifiers = mutableListOf<Identifier<out KtorModule>>()

    override fun processDeclarations(sequence: Sequence<Declaration<*>>) {
        modulesIdentifiers += sequence.map { it.identifier }.filterIsKclassSubclassOf()
    }
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
 * Retrieves all implementations of [KtorModule] subclasses in the given environment, sorted by decreasing priority.
 *
 * You should be able to use the output of this function as is in a for-each, like:
 *
 * ```kotlin
 * getKtorModulesByPriority(...).forEach { install() }
 * ```
 */
fun InjectionEnvironment.getKtorModulesByPriority(
    allIdentifiers: List<Identifier<out KtorModule>>,
    appName: String?
): List<KtorModule> {
    return allIdentifiers
        .map { get(it) }
        .filter { it.restrictToAppName == appName }
        .sortedByDescending { it.moduleInstallationPriority }
}
