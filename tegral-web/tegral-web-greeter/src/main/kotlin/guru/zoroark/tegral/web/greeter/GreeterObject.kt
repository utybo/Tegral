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

@file:Suppress("StringTemplate")

package guru.zoroark.tegral.web.greeter

import guru.zoroark.tegral.core.tegralVersion
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.invoke
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.di.extensions.ExtensibleInjectionEnvironment
import guru.zoroark.tegral.featureful.LifecycleHookedFeature
import guru.zoroark.tegral.logging.LoggerName
import guru.zoroark.tegral.logging.LoggingFeature
import org.slf4j.Logger

@Suppress("RemoveCurlyBracesFromTemplate")
private fun createMessage(): String {
    val version = tegralVersion
    val escape = 0x1b.toChar()
    val purpleFg = "$escape[38;2;175;76;254m"
    val reset = "$escape[0m"
    return """
        %${purpleFg}  __  _____      __ ${reset}
        %${purpleFg} / / |_   _|__  |_ |${reset}  Powered by ${purpleFg}Tegral${reset} | https://tegral.zoroark.guru
        %${purpleFg}| |    | |/ -_)  | |${reset}  Tegral version: ${purpleFg}$version${reset}
        %${purpleFg}| |    |_|\___|  | |${reset}          Issues: https://github.com/utybo/Tegral/issues
        %${purpleFg} \_\            |__|${reset}
    """.trimMargin("%")
}

@LoggerName("tegral.greeter")
internal class Greeter(scope: InjectionScope) {
    private val logger: Logger by scope()

    fun greet() {
        createMessage().lines().forEach { logger.info(it) }
    }
}

/**
 * A feature that automatically prints a "welcome message" before starting an application.
 */
object GreeterFeature : LifecycleHookedFeature {
    override val id = "tegral-web-greeter"
    override val name = "Tegral Web Greeter"
    override val description = "Simple feature that displays a message on startup"
    override val dependencies = setOf(LoggingFeature)

    override fun ExtensibleContextBuilderDsl.install() {
        put(::Greeter)
    }

    override fun beforeStart(env: ExtensibleInjectionEnvironment) {
        env.get<Greeter>().greet()
    }
}
