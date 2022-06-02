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

package guru.zoroark.tegral.di.full

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.dsl.tegralDi
import guru.zoroark.tegral.di.environment.InjectionScope
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.invoke
import kotlin.test.Test
import kotlin.test.assertEquals

// This is an example of a simple Controller <--> Service <--> Repository setup

class SimpleApplication {
    class Repository {
        private var storage: String = "Unset"
        fun record(value: String) {
            storage = value
        }

        fun retrieve(): String {
            return storage
        }
    }

    class Service(scope: InjectionScope) {
        private val repo: Repository by scope()
        fun getElement(): String {
            return repo.retrieve()
        }

        fun setElement(str: String) {
            repo.record(str)
        }
    }

    class Controller(scope: InjectionScope) {
        private val service: Service by scope()

        fun makeElementHtml(): String {
            return "<p>${service.getElement()}</p>"
        }

        fun setElement(newValue: String) {
            service.setElement(newValue)
        }
    }

    @Test
    fun `Test simple CSR model`() {
        val env = tegralDi {
            put { Controller(scope) }
            put(::Service)
            put(::Repository)
        }
        checkController(env.get())
        checkService(env.get())
        checkRepository(env.get())
    }

    private fun checkController(controller: Controller) {
        assertEquals("<p>Unset</p>", controller.makeElementHtml())
        controller.setElement("Hello!")
        assertEquals("<p>Hello!</p>", controller.makeElementHtml())
    }

    private fun checkService(service: Service) {
        assertEquals("Hello!", service.getElement())
        service.setElement("Goodbye!")
        assertEquals("Goodbye!", service.getElement())
    }

    private fun checkRepository(repository: Repository) {
        assertEquals("Goodbye!", repository.retrieve())
        repository.record("Goodbye for real!")
        assertEquals("Goodbye for real!", repository.retrieve())
    }
}
