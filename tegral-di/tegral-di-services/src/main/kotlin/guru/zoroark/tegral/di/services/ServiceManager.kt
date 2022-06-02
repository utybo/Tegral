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

package guru.zoroark.tegral.di.services

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.ExtensionNotInstalledException
import guru.zoroark.tegral.di.TegralDiException
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.getOrNull
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.extensions.DeclarationsProcessor
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import guru.zoroark.tegral.services.api.TegralService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Locale
import kotlin.reflect.full.isSubclassOf
import kotlin.system.measureTimeMillis

private enum class OperationType(val confirmationWord: String, val ingWord: String) {
    Start("started", "starting"),
    Stop("stopped", "stopping")
}

private fun OperationType.isBlockedByPolicy(policy: IgnorePolicy?): Boolean {
    return when (policy) {
        null -> false
        IgnorePolicy.IgnoreAll -> true
        IgnorePolicy.IgnoreStart -> this == OperationType.Start
        IgnorePolicy.IgnoreStop -> this == OperationType.Stop
    }
}

/**
 * Exceptions that occur within the starting or stopping process are wrapped with this type.
 */
class TegralServiceException(message: String, cause: Throwable) : TegralDiException(message, cause)

/**
 * Class for the [services extension][useServices] logic.
 *
 * This class is injected in extensible environments' meta-environment. You can retrieve this class by using
 * [services] on an extensible environment.
 */
class ServiceManager(scope: InjectionScope) : DeclarationsProcessor {
    private val environment: ExtensibleInjectionEnvironment by scope()
    private val ignorePolicies = mutableMapOf<Identifier<*>, IgnorePolicy>()

    private fun getServices(operationType: OperationType): Sequence<Pair<Identifier<*>, TegralService>> =
        environment.getAllIdentifiers()
            .filter { it.kclass.isSubclassOf(TegralService::class) }
            .filterNot { operationType.isBlockedByPolicy(ignorePolicies[it]) }
            .map {
                @Suppress("UNCHECKED_CAST")
                it to environment.get(it as Identifier<TegralService>)
            }

    /**
     * Starts all the [TegralService] components registered in this environment.
     *
     * Services [tagged][guru.zoroark.tegral.di.extensions.DeclarationTag] with [noService]/[IgnorePolicy.IgnoreAll] or
     * [noServiceStart]/[IgnorePolicy.IgnoreStart] are ignored and do not get started when calling this function.
     */
    suspend fun startAll(
        messageHandler: (String) -> Unit = { /* no-op */ }
    ): Map<Identifier<*>, Long> =
        doForEachDeclaration(
            OperationType.Start,
            messageHandler
        ) { it.start() }

    /**
     * Stops all the [TegralService] components registered in this environment.
     *
     * Services [tagged][guru.zoroark.tegral.di.extensions.DeclarationTag] with [noService]/[IgnorePolicy.IgnoreAll] or
     * [noServiceStop]/[IgnorePolicy.IgnoreStop] are ignored and do not get started when calling this function.
     */
    suspend fun stopAll(
        messageHandler: (String) -> Unit = { /* no-op */ }
    ): Map<Identifier<*>, Long> =
        doForEachDeclaration(
            OperationType.Stop,
            messageHandler
        ) { it.stop() }

    override fun processDeclarations(sequence: Sequence<Declaration<*>>) {
        sequence.forEach { declaration ->
            declaration.tags
                .filterIsInstance<IgnorePolicy>()
                // Combine all policies into one, see the + operator definition.
                .fold<IgnorePolicy, IgnorePolicy?>(null) { initial, next ->
                    (initial ?: next) + next
                }
                ?.let { ignorePolicies[declaration.identifier] = it }
        }
    }

    private suspend fun doForEachDeclaration(
        operationType: OperationType,
        messageHandler: (String) -> Unit,
        onService: suspend (TegralService) -> Unit
    ): Map<Identifier<*>, Long> = coroutineScope {
        val toAwait = getServices(operationType).map { (identifier, service) ->
            async {
                catching(operationType, identifier) {
                    val timeTaken = measureTimeMillis { onService(service) }
                    messageHandler("Service $identifier ${operationType.confirmationWord} in $timeTaken ms")
                    identifier to timeTaken
                }
            }
        }.toList()
        val result = toAwait.awaitAll()
        result.associateBy({ it.first }) { it.second }
    }

    @Suppress("TooGenericExceptionCaught") // Kind of the entire point here
    private inline fun <T> catching(operationType: OperationType, identifier: Identifier<*>, block: () -> T): T {
        try {
            return block()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            throw TegralServiceException(
                "${operationType.ingWord.capitalize()} service $identifier failed", e
            )
        }
    }

    private fun String.capitalize() =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }
}

/**
 * Installs the Services extension onto this extensible environment.
 *
 * This extension gives you the ability to start and stop *services* within your environment using the
 * [ServiceManager.startAll] and [ServiceManager.stopAll] functions.
 *
 * ### Creating services
 *
 * A service is just a component within a Tegrall DI environment that implements [TegralService].
 *
 * ### Starting and stopping services
 *
 * You can retrieve a [ServiceManager] instance from the environment after having installed this extension by using
 * [`env.services`][services] (where env is the environment). You can in turn use the service manager's
 * [startAll][ServiceManager.startAll] and [stopAll][ServiceManager.stopAll] functions to start and stop all the
 * services.
 *
 * ### Excluding services
 *
 * Services can be excluded from being started or stopped by tagging their declaration with the appropriate tag:
 *
 * - To fully ignore a service, use [noService]
 * - To ignore a service when starting, but not ignore when stopping, use [noServiceStart]
 * - To ignore a service when stopping, but not ignore when starting, use [noServiceStop]
 */
@TegralDsl
fun ExtensibleContextBuilderDsl.useServices() {
    meta { put(::ServiceManager) }
}

/**
 * Retrieves the [ServiceManager] that was created when running [useServices] while building the environment.
 *
 * Throws an exception if the extension is not currently installed.
 */
val ExtensibleInjectionEnvironment.services: ServiceManager
    get() = metaEnvironment.getOrNull() ?: throw ExtensionNotInstalledException(
        """
        Services extension is not installed.
        --> Building a full Tegral application?
            Use 'install(ServicesFeature)' (from tegral-services-feature) in your 'tegral' block.
        --> Just using Tegral DI?
            Install the service manager by adding 'useServices()' in your 'tegralDi' block.
        """.trimIndent()
    )
