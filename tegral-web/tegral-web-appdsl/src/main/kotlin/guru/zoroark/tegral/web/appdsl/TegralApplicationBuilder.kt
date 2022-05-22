package guru.zoroark.tegral.web.appdsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.di.dsl.ContextBuilderDsl
import guru.zoroark.tegral.di.dsl.EnvironmentContextBuilderDsl
import guru.zoroark.tegral.di.environment.Declaration

class TegralFeatureBuilder : ContextBuilderDsl, Buildable<TegralApplication> {
    private val environmentContextBuilder = EnvironmentContextBuilderDsl()

    override fun <T : Any> put(declaration: Declaration<T>) {
        environmentContextBuilder.put(declaration)
    }

    override fun build(): TegralApplication {
        TODO("Not yet implemented")
    }
}

class TegralApplicationBuilder : TegralApplicationDsl {
    private val defaultFeatureBuilder: TegralFeatureBuilder = TegralFeatureBuilder()

    override fun <T : Any> put(declaration: Declaration<T>) {
        defaultFeatureBuilder.put(declaration)
    }

}
