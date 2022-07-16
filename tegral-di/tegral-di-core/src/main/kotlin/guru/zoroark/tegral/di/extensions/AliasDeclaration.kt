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

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EmptyQualifier
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.Qualifier
import guru.zoroark.tegral.di.environment.ResolvableDeclaration
import guru.zoroark.tegral.di.environment.resolvers.AliasIdentifierResolver
import guru.zoroark.tegral.di.environment.resolvers.IdentifierResolver
import kotlin.reflect.KClass

/**
 * Declaration for aliases. Use [putAlias] to create such an alias.
 */
class AliasDeclaration<TAlias : Any, TTarget : TAlias>(
    aliasIdentifier: Identifier<TAlias>,
    /**
     * The identifier for the target for this alias.
     */
    val targetIdentifier: Identifier<TTarget>
) : ResolvableDeclaration<TAlias>(aliasIdentifier) {
    override fun buildResolver(): IdentifierResolver<TAlias> =
        AliasIdentifierResolver(targetIdentifier)
}

/**
 * Adds an alias to another component within this environment.
 *
 * For example:
 *
 * ```kotlin
 * interface FooContract
 * class FooImpl
 *
 * val env = tegralDi {
 *     put(::FooImpl)
 *     put<FooContract, FooImpl>()
 * }
 * val contract = env.get<FooContract>() // OK!
 * val impl = env.get<FooImpl>() // OK!
 * contract === impl // true
 * ```
 *
 * This overload is the inline-reified version. For non-reifiable use cases, see the KClass-based overload.
 */
@TegralDsl
inline fun <reified TAlias : Any, reified TTarget : TAlias> ContextBuilderDsl.putAlias(
    aliasQualifier: Qualifier = EmptyQualifier,
    targetQualifier: Qualifier = EmptyQualifier
): Declaration<TAlias> =
    putAlias(TAlias::class, aliasQualifier, TTarget::class, targetQualifier)

/**
 * Adds an alias to another component within this environment.
 *
 * For example:
 *
 * ```kotlin
 * interface FooContract
 * class FooImpl
 *
 * val env = tegralDi {
 *     put(::FooImpl)
 *     put(aliasClass = FooContract::class, targetClass = FooImpl::class)
 * }
 * val contract = env.get<FooContract>() // OK!
 * val impl = env.get<FooImpl>() // OK!
 * contract === impl // true
 * ```
 *
 * This is the KClass-based overload. For most cases, you should use the inline-reified version instead.
 */
@TegralDsl
fun <TAlias : Any, TTarget : TAlias> ContextBuilderDsl.putAlias(
    aliasClass: KClass<TAlias>,
    aliasQualifier: Qualifier = EmptyQualifier,
    targetClass: KClass<TTarget>,
    targetQualifier: Qualifier = EmptyQualifier
): Declaration<TAlias> =
    AliasDeclaration(Identifier(aliasClass, aliasQualifier), Identifier(targetClass, targetQualifier))
        .also { put(it) }
