package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.Token
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.StoreStateCallback
import org.yaml.snakeyaml.parser.ParserException
import kotlin.reflect.typeOf


/**
 * Class for a full parser that is ready to parse a chain of tokens.
 *
 * @param T The type of the *root node*, the node at the root of the abstract
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
        ExpectedNode(rootType, StoreStateCallback(rootKey))

    private val typeMap: Map<ParserNodeDeclaration<*>, DescribedType<*>> =
        types.associateBy { it.type }

    /**
     * Launch the parser on the given tokens.
     */
    fun parse(tokens: List<Token>): T {
        when (val result = parseToResult(tokens, false)) {
            is ParserResult.Failure -> throw NiwenParserException(result.reason)
            is ParserResult.Success -> return result.result
        }

    }

    private fun parseToResult(tokens: List<Token>, enableDebugger: Boolean): ParserResult<T> {
        val context = ParsingContext(tokens, typeMap, if (enableDebugger) BranchSeeker() else null)
        val result = rootExpectation.matches(context, 0)
        fun finishDebugger(status: BranchSeeker.Status, message: String): String {
            context.branchSeeker?.updateRoot(
                status,
                message,
                (result as? ExpectationResult.Success)?.stored ?: emptyMap()
            )
            return context.branchSeeker?.toYamlRepresentation() ?: "Debugger was not enabled"
        }

        @Suppress("UNCHECKED_CAST")
        return when (result) {
            is ExpectationResult.DidNotMatch ->
                "Parsing failed: ${result.message} (token nb ${result.atTokenIndex}".let { msg ->
                    ParserResult.Failure(msg, finishDebugger(BranchSeeker.Status.DID_NOT_MATCH, msg))
                }

            is ExpectationResult.Success -> {
                if (result.nextIndex != tokens.size) {
                    "Parsing stopped, but not all tokens were consumed. Stopped at index ${result.nextIndex} while there are ${tokens.size} tokens".let { msg ->
                        ParserResult.Failure(msg, finishDebugger(BranchSeeker.Status.DID_NOT_MATCH, msg))
                    }

                } else {
                    val obj = result.stored[rootKey] as? T
                        ?: error("Internal error: the root result was not stored. Please report this.")
                    ParserResult.Success(obj, finishDebugger(BranchSeeker.Status.SUCCESS, "Parsing successful"))
                }
            }
        }
    }

    sealed class ParserResult<T>(val debuggerResult: String) {
        class Success<T>(val result: T, debuggerResult: String) : ParserResult<T>(debuggerResult)
        class Failure<T>(val reason: String, debuggerResult: String) : ParserResult<T>(debuggerResult)
    }

    fun parseWithDebugger(tokens: List<Token>): ParserResult<T> {
        return parseToResult(tokens, true)
    }
}
