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

package guru.zoroark.tegral.di.extensions

import guru.zoroark.tegral.di.InternalErrorException
import guru.zoroark.tegral.di.environment.EnvironmentContext
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.InjectionEnvironment
import guru.zoroark.tegral.di.environment.InjectionEnvironmentKind
import guru.zoroark.tegral.di.environment.ScopedSupplierDeclaration
import guru.zoroark.tegral.di.environment.resolvers.IdentifierResolver
import kotlin.reflect.full.isSubclassOf

/**
 * An extensible injection environment is an injection environment that supports extensions.
 *
 * While some additional features (such as [factories][guru.zoroark.tegral.di.extensions.factory.putFactory]) can be
 * implemented purely using Tegral DI's public API, some extensions simply cannot do this and require:
 *
 * - Analysis of declarations outside of tests.
 * - State storage when just putting more stuff within the environment is not feasible.
 *
 * ### Meta-environments
 *
 * Extensible environments are, at their core, environments with an additional sub-environment named the "meta
 * environment". By default, meta-environments are [EagerImmutableMetaEnvironment] objects. This meta environment can be
 * used to:
 *
 * - Store additional states (via regular [put][guru.zoroark.tegral.di.dsl.put]/[get][InjectionEnvironment.get]/
 * [inject][InjectionEnvironment.createInjector] mechanisms).
 * - [Process declarations][DeclarationsProcessor] that were put in the regular environment.
 *
 * You should not implement this interface directly yourself: subclass [DefaultExtensibleInjectionEnvironment] instead,
 * which already handles all the extensions-related logic.
 *
 * ### Conventions
 *
 * Extensible injection environments should follow the same conventions as regular injection environments, especially
 * for documentation and usage. However, you **must not** use [InjectionEnvironmentKind] for extension environments --
 * otherwise, the DSL will not see your environment as extensible. Use [ExtensibleInjectionEnvironmentKind] instead.
 *
 * In order to uniformly document extensibility, implementors should include the following line in their KDoc comment:
 *
 * > Compatible with installable extensions.
 */
interface ExtensibleInjectionEnvironment : InjectionEnvironment {
    /**
     * The meta environment for this environment.
     *
     * The meta environment *must not* be extensible itself.
     */
    val metaEnvironment: InjectionEnvironment

    /**
     * Returns the resolver associated with the given identifier.
     */
    fun <T : Any> getResolverOrNull(identifier: Identifier<T>): IdentifierResolver<T>?
}

/**
 * Default implementation for [ExtensibleInjectionEnvironment].
 */
abstract class DefaultExtensibleInjectionEnvironment(
    context: ExtensibleEnvironmentContext,
    metaContextKind: InjectionEnvironmentKind<*>
) : ExtensibleInjectionEnvironment {
    override val metaEnvironment = createMetaEnvironment(context, metaContextKind)
}

/**
 * Performs the internal logic required for setting up a meta-environment. When creating extensible environments, you
 * should either call this function to create your meta environment, or subclass [DefaultExtensibleInjectionEnvironment]
 * which will do it for you.
 */
fun <T : InjectionEnvironment> ExtensibleInjectionEnvironment.createMetaEnvironment(
    context: ExtensibleEnvironmentContext,
    metaContextKind: InjectionEnvironmentKind<T>
): T {
    val metaEnvironment = metaContextKind.build(
        context.metaContext.let {
            // Inject the EIE within the meta-environment
            val declaration = ScopedSupplierDeclaration(Identifier(ExtensibleInjectionEnvironment::class)) {
                this@createMetaEnvironment
            }
            val newDeclarations = it.declarations.toMutableMap()
            newDeclarations[declaration.identifier] = declaration
            EnvironmentContext(newDeclarations)
        }
    )

    context.metaContext.declarations.keys.filter {
        it.kclass.isSubclassOf(DeclarationsProcessor::class)
    }.forEach { processorIdentifier ->
        val processor = metaEnvironment.get(processorIdentifier) as? DeclarationsProcessor
            ?: throw InternalErrorException(
                "Internal error: processor has an identifier declaring it is a subclass of " +
                    "DeclarationsProcessor, but the actual object is not."
            )
        processor.processDeclarations(context.declarations.values.asSequence())
    }

    return metaEnvironment
}
