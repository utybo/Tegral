package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.Token
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import kotlin.reflect.typeOf


/**
 * Class for a full parser that is ready to parse a chain of tokens.
 *
 * @param R The type of the *root node*, the node at the root of the abstract
 * syntax tree
 *
 * @param types List of described types that will be used for the parsing
 * process. Each entry describes a type of node that can be encountered in the
 * AST.
 *
 * @param rootType The expected type of the root node.
 */
class NiwenParser<T>(
    types: List<DescribedType<*>>,
    private val rootType: ParserNodeDeclaration<T>
) {
    private val rootKey = NodeParameterKey<Nothing, T>(typeOf<Any?>(), "N/A")
    private val rootExpectation: Expectation<Nothing, T> =
        ExpectedNode(rootType, rootKey)

    private val typeMap: Map<ParserNodeDeclaration<*>, DescribedType<*>> =
        types.associateBy { it.type }

    /**
     * Launch the parser on the given tokens.
     */
    fun parse(tokens: List<Token>): T {
        val result = rootExpectation.matches(
            ParsingContext(tokens, typeMap),
            0
        )
        // TODO throw exception when result index != end of tokens
        //      (i.e. we didn't parse everything)
        @Suppress("UNCHECKED_CAST")
        return when (result) {
            is ExpectationResult.DidNotMatch ->
                throw NiwenParserException("Parsing failed: ${result.message} (token nb ${result.atTokenIndex})")

            is ExpectationResult.Success -> result.stored[rootKey] as? T
                ?: error("Internal error: the root result was not stored. Please report this.")
        }
    }
}
