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

package guru.zoroark.tegral.di.test.mockk

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.test.TestMutableInjectionEnvironment
import io.mockk.mockk
import kotlin.reflect.KClass

/**
 * Creates a MockK mock using the given settings and lambda, then puts it in the Tegral DI test environment.
 *
 * This is comparable to calling:
 *
 * ```kotlin
 * val result = mockk(...) { ... }
 * put(result)
 * ```
 */
inline fun <reified T : Any> TestMutableInjectionEnvironment.putMock(
    name: String? = null,
    relaxed: Boolean = false,
    vararg moreInterfaces: KClass<*>,
    relaxUnitFun: Boolean = false,
    mockSetup: T.() -> Unit
): T {
    val mock = mockk(
        name = name,
        relaxed = relaxed,
        moreInterfaces = moreInterfaces,
        relaxUnitFun = relaxUnitFun,
        block = mockSetup
    )
    put { mock }
    return mock
}
