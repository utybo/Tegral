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

    /**
     * Name for the provided node (usually the name of the class [T]).
     *
     * By default, this function does a call to [toString] on the declaration, which is not accurate but close enough to
     * provide useful debug data.
     */
    val name: String
        get() = this.toString()
}
