package guru.zoroark.tegral.web.appdsl

import com.sksamuel.hoplite.toml.TomlPropertySource
import guru.zoroark.tegral.config.core.RootConfig
import guru.zoroark.tegral.config.core.TegralConfig
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.services.feature.ServicesFeature
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// Repro for https://github.com/utybo/Tegral/issues/39
class Issue39Repro {
    data class MyConfig(
            override val tegral: TegralConfig = TegralConfig(mapOf()),
            val hello: String,
            val foo: FooConfig
    ) : RootConfig

    data class FooConfig(val bar: String)

    // Example usage in a regular class:

    class FoobarService(scope: InjectionScope) {
        private val config: MyConfig by scope()
        val rootConfig: RootConfig by scope()

        fun whatIsBar(): String {
            return config.foo.bar
        }
    }

    @Test
    fun `Test issue 39`() = runBlocking {
        val app = tegral(enableDefaults = false) {
            install(ServicesFeature)

            useConfiguration<MyConfig> {
                addSource(
                    TomlPropertySource(
                        """
                        hello = "Hello!"
                        [foo]
                        bar = "Bar!"
                        """.trimIndent()
                    )
                )
            }

            put(::FoobarService)
        }
        val config = app.environment.get<MyConfig>()
        val service = app.environment.get<FoobarService>()
        assertEquals("Hello!", config.hello)
        assertEquals("Bar!", config.foo.bar)
        assertEquals("Bar!", service.whatIsBar())
        assertSame(config, service.rootConfig)
    }
}
