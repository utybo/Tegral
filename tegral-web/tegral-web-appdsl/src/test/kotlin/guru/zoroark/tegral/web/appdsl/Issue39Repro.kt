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

            useConfigurationType<MyConfig> {
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
