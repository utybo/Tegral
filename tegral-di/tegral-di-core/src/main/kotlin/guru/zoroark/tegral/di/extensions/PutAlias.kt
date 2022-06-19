package guru.zoroark.tegral.di.extensions

import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.environment.AliasIdentifierResolver
import guru.zoroark.tegral.di.environment.Declaration
import guru.zoroark.tegral.di.environment.EmptyQualifier
import guru.zoroark.tegral.di.environment.Identifier
import guru.zoroark.tegral.di.environment.IdentifierResolver
import guru.zoroark.tegral.di.environment.Qualifier
import guru.zoroark.tegral.di.environment.ResolvableDeclaration
import kotlin.reflect.KClass

class AliasDeclaration<TAlias : Any, TTarget : TAlias>(
    aliasIdentifier: Identifier<TAlias>,
    private val targetQualifier: Identifier<TTarget>
) : ResolvableDeclaration<TAlias>(aliasIdentifier) {
    override fun buildResolver(): IdentifierResolver<TAlias> =
        AliasIdentifierResolver(targetQualifier)
}

@TegralDsl
inline fun <reified TAlias : Any, reified TTarget : TAlias> ContextBuilderDsl.putAlias(
    aliasQualifier: Qualifier = EmptyQualifier,
    targetQualifier: Qualifier = EmptyQualifier
) : Declaration<TAlias> =
    putAlias(TAlias::class, aliasQualifier, TTarget::class, targetQualifier)

@TegralDsl
fun <TAlias : Any, TTarget : TAlias> ContextBuilderDsl.putAlias(
    aliasClass: KClass<TAlias>,
    aliasQualifier: Qualifier = EmptyQualifier,
    targetClass: KClass<TTarget>,
    targetQualifier: Qualifier = EmptyQualifier
): Declaration<TAlias> =
    AliasDeclaration<TAlias, TTarget>(Identifier(aliasClass, aliasQualifier), Identifier(targetClass, targetQualifier))
