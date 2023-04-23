package guru.zoroark.tegral.niwen.parser.dsl

import guru.zoroark.tegral.core.Buildable
import guru.zoroark.tegral.core.TegralDsl
import guru.zoroark.tegral.niwen.parser.DescribedType
import guru.zoroark.tegral.niwen.parser.NiwenParser
import guru.zoroark.tegral.niwen.parser.NiwenParserException
import guru.zoroark.tegral.niwen.parser.ParserNodeDeclaration

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
        root(this)
        this(block)
    }

    @TegralDsl
    fun root(decl: ParserNodeDeclaration<T>) {
        if (rootNodeType != null) {
            throw NiwenParserException("Another node was already defined as the root, ${this::class} cannot also be a root.")
        }
        rootNodeType = decl
    }

    /**
     * Provides a way to group declaration together.
     *
     * This has no effect; use this function to better organize your [niwenParser] block.
     */
    @TegralDsl
    infix fun String.group(block: NiwenParserBuilder<T>.() -> Unit) {
        block()
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
@TegralDsl
fun <T> niwenParser(block: NiwenParserBuilder<T>.() -> Unit): NiwenParser<T> =
    NiwenParserBuilder<T>().apply(block).build()
