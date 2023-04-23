package guru.zoroark.tegral.niwen.parser.expectations

import guru.zoroark.tegral.niwen.parser.ExpectationResult
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration
import guru.zoroark.tegral.niwen.parser.ParsingContext
import guru.zoroark.tegral.niwen.parser.TypeDescription

/**
 * General class for an expectation.
 *
 * Expectations are the core of the parsing algorithms used by Niwen Parser, and a
 * way to express what we *expect* the chain of tokens to look like in order to
 * be parsed into an abstract syntax tree.
 *
 * Each node of the tree is described as a list of expectations over a chain of
 * token. In other words, constructing a node is as simple as just checking if
 * the list of expectations matches the chain of tokens.
 *
 * Recognizing tokens is not just about expectations: we also need to store the
 * matched value of some expectations. Take a string for example:
 *
 *  ```
 *  "Hello World!"
 *  ```
 *
 * While the quotation marks (`"`) are not useful for the abstract syntax tree
 * (the fact that this is a string will be represented by having a node type
 * specific to string values), the content of the string is very important.
 * Therefore, expectations also know where their matched value should be stored
 * through the [storeValueIn] property. This stored value can then be retrieved
 * through the [TypeDescription]'s
 * [arguments][TypeDescription.arguments] and are passed using the [make
 * function][ParserNodeDeclaration.make] in the [ParserNodeDeclaration].
 *
 * If [storeValueIn] is `null`, that means that:
 *
 * - this expectation cannot store value (it does not "emit" anything, like the
 * `either` construct which just executes branches and picks the first result),
 * or
 *
 * - this expectation can store values but we do not want to store it for this
 * expectation
 *
 * Parser algorithms assume that expectations always expect some tokens to be
 * present (e.g. if we run out of tokens, we fail immediately instead of letting
 * the expectation crash). If the expectation can handle situations where there
 * are not enough tokens, then it should implement the [HandlesTokenDrought]
 * marker interface.
 *
 * @param T Type this expectation is ran in.
 * @param R Result this expectation will produce. For explanations on the
 * variance of this type, see [NodeParameterKey]
 */
abstract class Expectation<T, in R>(
    /**
     * The name of the argument where the result of this expectation should be
     * stored, or `null` if the matched value of this expectation should not or
     * cannot be stored.
     */
    val stateCallback: StateCallback<T, R, *>? = null
) {
    /**
     * Check if this expectation matches the given context at the given index
     * among the [context's tokens list][ParsingContext.tokens].
     *
     * The index is guaranteed to be in the bounds of the context's tokens,
     * unless the expectation signals that it can handle OOB tokens (i.e. it
     * implements [HandlesTokenDrought]).
     */
    abstract fun matches(
        context: ParsingContext,
        index: Int
    ): ExpectationResult<T>

    abstract val title: String
}

/**
 * When an expectation is also of type HandlesTokenDrought, it means that the
 * expectation should be called even when running out of tokens.
 *
 * Marker interface: does not require you to implement anything.
 */
interface HandlesTokenDrought
