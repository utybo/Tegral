package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.Token

/**
 * This object contains the information that is passed to expectations, and is
 * global over a single parser run.
 *
 * @property tokens The list of tokens that should be parsed
 *
 * @property typeMap A map with all of the known declared types and their
 * description
 */
class ParsingContext(
    val tokens: List<Token>,
    val typeMap: Map<ParserNodeDeclaration<*>, DescribedType<*>>
) {
    operator fun <T> get(declaration: ParserNodeDeclaration<T>): DescribedType<T>? {
        return typeMap[declaration] as? DescribedType<T>
    }
}