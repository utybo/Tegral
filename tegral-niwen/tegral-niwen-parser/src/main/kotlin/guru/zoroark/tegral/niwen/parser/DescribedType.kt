package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.parser.expectations.Expectation

/**
 * A node type declaration (providing a way to make the actual type) with its
 * descriptor (which declares what the type expects).
 *
 * @property type The type described
 *
 * @property expectations The descriptor: currently, just a list of the
 * expectations that make up this type
 */
class DescribedType<T>(
    val type: ParserNodeDeclaration<T>,
    val expectations: List<Expectation<T, *>>
)