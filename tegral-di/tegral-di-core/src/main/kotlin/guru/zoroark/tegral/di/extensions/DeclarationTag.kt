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
import guru.zoroark.tegral.di.environment.Declaration

/**
 * A declaration tag is data added to a [Declaration] that can be processed by (installable) extensions via a
 * [DeclarationsProcessor].
 *
 * Declaration tags are attached to declarations using the [with] functions, e.g.
 *
 * ```
 * put(::Foo) with bar
 * put(::Baz) with hello
 * ```
 *
 * Declaration tags can come in any shape: it is up to the extension to decide what its tags actually are.
 */
interface DeclarationTag

/**
 * A list of declaration tags, used for easier DSL usage.
 */
class DeclarationTags {
    /**
     * The actual mutable list of tags.
     */
    val tags = mutableListOf<DeclarationTag>()
}

/**
 * Adds the given tag to this declaration.
 */
@TegralDsl
infix fun <T : Any> Declaration<T>.with(tag: DeclarationTag): Declaration<T> =
    apply { tags += tag }

/**
 * Adds the given tags to this declaration.
 *
 * Note that you can also chain the addition of tags using `+`.
 *
 * ```
 * put(::Foo) with bar + baz
 * ```
 */
@TegralDsl
infix fun <T : Any> Declaration<T>.with(tags: List<DeclarationTag>): Declaration<T> =
    apply { this.tags += tags }

/**
 * Adds the given tags to this declaration.
 */
@TegralDsl
infix fun <T : Any> Declaration<T>.with(tags: DeclarationTags): Declaration<T> =
    with(tags.tags)

/**
 * Puts two tags together into a new [DeclarationTags] instance.
 */
operator fun DeclarationTag.plus(otherTag: DeclarationTag) =
    DeclarationTags().apply {
        tags.add(this@plus)
        tags.add(otherTag)
    }

/**
 * Convenience function for adding the given tag to this [DeclarationTags] object, then returns said object.
 */
operator fun DeclarationTags.plus(otherTag: DeclarationTag) =
    apply { tags.add(otherTag) }
