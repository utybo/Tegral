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

import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.di.environment.get
import guru.zoroark.tegral.di.environment.named
import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl
import guru.zoroark.tegral.featureful.Feature
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
class FeatureInstallationTest {
    object TestFeature : Feature<TestFeature.TestFeatureConfigObj> {
        data class TestFeatureConfigObj(
            var valueOne: String? = null,
            var valueTwo: Int? = null
        )

        override val id = "test-feature"
        override val name = "Test Feature"
        override val description = "My test feature"

        override fun createConfigObject(): TestFeatureConfigObj = TestFeatureConfigObj()

        override fun ExtensibleContextBuilderDsl.install(configuration: TestFeatureConfigObj) {
            put(named("one")) { configuration.valueOne!! }
            put(named("two")) { configuration.valueTwo!! }
        }
    }

    @Test
    fun `Install feature with configuration block`() {
        val builder = TegralApplicationBuilder().apply {
            install(TestFeature) {
                valueOne = "ONE!!!"
                valueTwo = 222
            }
        }
        val env = builder.build().environment
        assertEquals("ONE!!!", env.get<String>(named("one")))
        assertEquals(222, env.get<Int>(named("two")))
    }
}
