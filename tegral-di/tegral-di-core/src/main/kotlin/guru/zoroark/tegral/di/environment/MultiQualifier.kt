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

package guru.zoroark.tegral.di.environment

import guru.zoroark.tegral.di.InvalidDeclarationException

/**
 * A multi-qualifier represents multiple qualifiers grouped as a set. This is useful for combining qualifiers (e.g.
 * a named qualifier and a type qualifier).
 *
 * Note that multi-qualifier accepts all elements within its set *except* other multi-qualifiers (nesting is not
 * supported and multi-qualifiers should be "flat") or empty qualifiers (which do not contribute any information).
 *
 * @property qualifiers The qualifiers represented by this MultiQualifier.
 */
class MultiQualifier(val qualifiers: Set<Qualifier>) : Qualifier {
    init {
        if (qualifiers.size < 2) {
            throw InvalidDeclarationException(
                "A MultiQualifier with less than 2 qualifiers is useless." +
                    "For 0 qualifiers, use EmptyQualifier instead, for 1 qualifier, use that qualifier as-is."
            )
        }
        if (qualifiers.contains(EmptyQualifier)) {
            throw InvalidDeclarationException("MultiQualifiers must not contain EmptyQualifier.")
        }

        if (qualifiers.any { it is MultiQualifier }) {
            throw InvalidDeclarationException("MultiQualifiers cannot be nested.")
        }
    }

    override fun toString(): String = qualifiers.joinToString(" + ")

    override fun equals(other: Any?): Boolean = other is MultiQualifier && other.qualifiers == qualifiers

    override fun hashCode(): Int = qualifiers.hashCode()
}

/**
 * Combines this qualifier with the other.
 *
 * If neither of the operands are [EmptyQualifier], returns a [MultiQualifier]. If one or the other is an
 * [EmptyQualifier], returns the non-empty qualifier. If both are empty qualifiers, returns [EmptyQualifier].
 */
operator fun Qualifier.plus(other: Qualifier): Qualifier = when {
    // EmptyQualifier does not contribute anything and needs to be filtered out.
    // (first case also handles the case where this *and* other are EmptyQualifier)
    this is EmptyQualifier -> other
    other is EmptyQualifier -> this

    // Handle cases where this, other or both are multi-qualifiers
    this is MultiQualifier && other is MultiQualifier -> MultiQualifier(this.qualifiers union other.qualifiers)
    this is MultiQualifier -> MultiQualifier(this.qualifiers.toMutableSet().apply { add(other) })
    other is MultiQualifier -> MultiQualifier(other.qualifiers.toMutableSet().apply { add(this@plus) })

    // Otherwise, combine both
    else -> MultiQualifier(setOf(this, other))
}
