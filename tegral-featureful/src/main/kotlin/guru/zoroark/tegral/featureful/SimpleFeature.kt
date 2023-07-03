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

package guru.zoroark.tegral.featureful

import guru.zoroark.tegral.di.extensions.ExtensibleContextBuilderDsl

/**
 * A simplified interface for [Feature] that cannot be configured.
 *
 * In reality, the type of the configuration is [Unit]. If you need to use [SimpleFeature] in combination with
 * [ConfigurableFeature] or other specializations of [Feature], use `Unit` as the type parameter.
 */
interface SimpleFeature : Feature<Unit> {
    override fun createConfigObject() = Unit

    /**
     * Identical to [Feature.install], but without a configuration object.
     */
    fun ExtensibleContextBuilderDsl.install()

    override fun ExtensibleContextBuilderDsl.install(configuration: Unit) {
        install()
    }
}
