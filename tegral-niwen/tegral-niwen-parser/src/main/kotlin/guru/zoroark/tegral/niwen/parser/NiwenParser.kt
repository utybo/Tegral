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

package guru.zoroark.tegral.niwen.parser

import guru.zoroark.tegral.niwen.lexer.Token
import guru.zoroark.tegral.niwen.parser.expectations.Expectation
import guru.zoroark.tegral.niwen.parser.expectations.ExpectedNode
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey
import guru.zoroark.tegral.niwen.parser.expectations.StoreStateCallback
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
    private val rootKey = NodeParameterKey<Nothing, T>(typeOf<Any?>(), "Parser root result")
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

    fun parseToResult(tokens: List<Token>, enableDebugger: Boolean): ParserResult<T> {
        val context = ParsingContext(tokens, typeMap, if (enableDebugger) BranchSeeker() else null)
        val result = rootExpectation.matches(context, 0)
        fun finishDebugger(status: BranchSeeker.Status, message: String): String {
            context.branchSeeker?.updateRoot(
                status,
                message,
                (result as? ExpectationResult.Success)?.stored.orEmpty()
            )
            return context.branchSeeker?.toYamlRepresentation() ?: "Debugger was not enabled"
        }

        @Suppress("UNCHECKED_CAST")
        return when (result) {
            is ExpectationResult.DidNotMatch -> {
                val message = "Parsing failed: ${result.message} (token nb ${result.atTokenIndex}"
                ParserResult.Failure(message, finishDebugger(BranchSeeker.Status.DID_NOT_MATCH, message))
            }

            is ExpectationResult.Success -> {
                if (result.nextIndex != tokens.size) {
                    val message = "Parsing stopped, but not all tokens were consumed. Stopped at index " +
                        "${result.nextIndex} while there are ${tokens.size} tokens"
                    ParserResult.Failure(message, finishDebugger(BranchSeeker.Status.DID_NOT_MATCH, message))
                } else {
                    val obj = result.stored[rootKey] as? T
                        // TODO change to regular failure and not an exception
                        ?: error("Internal error: the root result was not stored. Please report this.")
                    ParserResult.Success(obj, finishDebugger(BranchSeeker.Status.SUCCESS, "Parsing successful"))
                }
            }
        }
    }

    /**
     * Result of a parsing pass
     *
     * @property debuggerResult The resulting YAML from the debugger if enabled, or a short message which indicates that
     * the debugger was disabled
     *
     * @param T The resulting root object this parser produces.
     */
    sealed class ParserResult<T>(val debuggerResult: String) {
        /**
         * Successful parser result.
         *
         * @property result The resulting object
         */
        class Success<T>(val result: T, debuggerResult: String) : ParserResult<T>(debuggerResult) {
            override fun orThrow() = result

            override fun toString(): String = "ParserResult.Success(result = ${result})"
        }

        /**
         * Failed parser result. You can usually get a more in-depth analysis by enabling the debugger and getting the
         * [ParserResult.debuggerResult]
         *
         * @property reason Short reason message for the failure
         */
        class Failure<T>(val reason: String, debuggerResult: String) : ParserResult<T>(debuggerResult) {
            override fun orThrow() = throw NiwenParserException("Parsing failed: $reason")
            override fun toString(): String = "ParserResult.Failure(reason = $reason)"
        }

        abstract fun orThrow(): T
    }

    /**
     * Parse a list of token using this debugger.
     *
     * Does not throw if parsing fails; instead, this function returns a [ParserResult] object.
     * [ParserResult.debuggerResult] is guaranteed to be a YAML object with the debugger information.
     */
    fun parseWithDebugger(tokens: List<Token>): ParserResult<T> {
        return parseToResult(tokens, true)
    }
}
