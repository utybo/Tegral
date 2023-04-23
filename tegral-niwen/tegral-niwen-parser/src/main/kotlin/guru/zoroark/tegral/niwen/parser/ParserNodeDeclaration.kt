package guru.zoroark.tegral.niwen.parser

/**
 * A *node declaration*. This simple interface provides a way to turn the
 * description of a node into an actual node.
 *
 * In most cases, Niwen can determine which constructors to use using reflection. Use the following:
 *
 * ```kotlin
 * class MyNodeType(val child: MyOtherNodeType) {
 *     companion object : ParserNodeDeclaration<MyNodeType> by reflective()
 * }
 * ```
 *
 * You would typically implement it like so:
 * TODO this is wrong
 *
 *  ```
 *  class MyNodeType(val child: MyOtherNodeType) {
 *      companion object : ParserNodeDeclaration<MyNodeType> {
 *          override fun make(args: TypeDescription) =
 *              MyNodeType(args["child"])
 *      }
 *  }
 *  ```
 *
 *  @see make
 */
fun interface ParserNodeDeclaration<T> {
    /**
     * This function creates a node of type [T] from the arguments.
     *
     * The arguments contain everything that needed to be stored for the
     * creation of the node. If you stored something in the descriptor for this
     * node, it will be available in [args].
     *
     * @args The description of the node, which contains all of the stored
     * information for creating it.
     */
    fun make(args: TypeDescription<T>): T

    val nodeName: String?
        get() = null
}

val ParserNodeDeclaration<*>.name: String
    get() = nodeName ?: this.toString()
