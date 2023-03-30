package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.*
import kotlin.experimental.ExperimentalTypeInference

/**
 * DSL builder for a Niwen parser.
 */
@TegralDsl
class NiwenParserBuilder<T> : Buildable<NiwenParser<T>> {
    private var rootNodeType: ParserNodeDeclaration<T>? = null
    private val builtTypeDef = mutableListOf<DescribedType<*>>()
    private val knownDeclarations = mutableSetOf<ParserNodeDeclaration<*>>()

    /**
     * Creates a node declaration for the given node type with the given block
     * as its descriptor.
     */
    operator fun <T> ParserNodeDeclaration<T>.invoke(block: DescribedTypeBuilder<T>.() -> Unit) {
        if (this in knownDeclarations) {
            throw NiwenParserException("The node declaration ${this::class} was already described elsewhere: you cannot describe it twice.")
        }
        builtTypeDef += DescribedTypeBuilder(this).apply(block).build()
        knownDeclarations += this
    }

    /**
     * Similar to [invoke], but also signals that this node is the *root node*
     * of the constructed tree.
     */
    @TegralDsl
    infix fun ParserNodeDeclaration<T>.root(block: DescribedTypeBuilder<T>.() -> Unit) {
        if (rootNodeType != null) {
            throw NiwenParserException("Another node was already defined as the root, ${this::class} cannot also be a root.")
        }
        this(block)
        rootNodeType = this
    }

    /**
     * Build this parser
     */
    override fun build(): NiwenParser<T> =
        //  user to configure the root node.
        NiwenParser<T>(
            builtTypeDef,
            rootNodeType
                ?: throw NiwenParserException("You never defined a root node: please described a node with 'root' so the parser knows where to start!")
        )
}

/**
 * Main entry-point for the Niwen Parser DSL. A parser might typically look like this
 *
 *  ```
 *  val parser = niwenParser {
 *      MyNodeType {
 *          expect(tokenType)
 *          expect(otherTokenType) storeIn "hello"
 *      }
 *
 *      MyOtherNodeType root {
 *          expect(someToken)
 *          expect(MyNodeType) storeIn "theNode"
 *      }
 *  }
 *  parser.parse(tokens)
 *  ```
 */
@OptIn(ExperimentalTypeInference::class)
@TegralDsl
fun <T> niwenParser(@BuilderInference block: NiwenParserBuilder<T>.() -> Unit): NiwenParser<T> =
    NiwenParserBuilder<T>().apply(block).build()