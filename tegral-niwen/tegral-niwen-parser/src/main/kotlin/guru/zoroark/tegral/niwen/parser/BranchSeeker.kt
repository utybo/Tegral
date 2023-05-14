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

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import guru.zoroark.tegral.niwen.parser.expectations.NodeParameterKey

private val DEBUGGER_MAPPER =
    YAMLMapper(
        YAMLFactory.builder()
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .disable(YAMLGenerator.Feature.SPLIT_LINES)
            .build()
    ).registerKotlinModule()

/**
 * A utility for reporting the execution tree of a parsing run, and outputting it in a human-readable format.
 */
class BranchSeeker {
    /**
     * Status for a node in the [BranchSeeker] tree.
     */
    enum class Status { SUCCESS, DID_NOT_MATCH }

    /**
     * A node within the execution tree. Each node represents an expectation that was executed.
     */
    data class Node(
        /**
         * Parent for this node, or null if this is the root node.
         */
        val parent: Node?,
        /**
         * Title for this node.
         *
         * This title should be independent of the result of the expectation this node represents. For example, this
         * could be `expect(SomeNode)`
         */
        var title: String,
        /**
         * Status for this node
         */
        var status: Status? = null,
        /**
         * Message for this node. Usually a short explanation of why this expectation was successful or failed.
         */
        var message: String? = null,
        /**
         * The sub-nodes that were executed as part of this expectation
         */
        val nodes: MutableList<Node> = mutableListOf(),
        /**
         * A map of data that was stored using `storeIn` in this node.
         */
        val storedData: MutableMap<NodeParameterKey<*, *>, Any?> = mutableMapOf()
    ) {
        /**
         * Create a child node for this node.
         *
         * @return The created child
         */
        fun createChild(title: String): Node {
            val child = Node(this, title)
            nodes += child
            return child
        }
    }

    private val root = Node(null, "Root")
    private var current = root

    /**
     * Step into a new node.
     *
     * This node is created as a child of the current node with the given title.
     */
    fun stepIn(nodeTitle: String) {
        current = current.createChild(nodeTitle)
    }

    /**
     * Steps out of the current node.
     *
     * The current node is updated with the given status, message and stored data. The current node is set to the parent
     * of this node.
     *
     * If the current node does not have a parent, i.e. the current node is the root node, an exception is thrown.
     */
    fun stepOut(status: Status, message: String, storedData: Map<NodeParameterKey<*, *>, Any?>) {
        val parent = requireNotNull(current.parent)
        current.status = status
        current.message = message
        current.storedData.putAll(storedData)
        current = parent
    }

    /**
     * Update the root node with the given information.
     */
    fun updateRoot(status: Status, message: String, storedData: Map<NodeParameterKey<*, *>, Any?>) {
        root.status = status
        root.message = message
        root.storedData.putAll(storedData)
    }

    /**
     * Transforms this branch seeker into a human-readable YAML representation as a string.
     */
    fun toYamlRepresentation(): String {
        fun Status?.toIcon(): String = when (this) {
            null -> "❔"
            Status.SUCCESS -> "✅"
            Status.DID_NOT_MATCH -> "❌"
        }

        fun Node.toMap(): LinkedHashMap<String, Any> {
            val result = linkedMapOf<String, Any>()
            result[title] = "${status.toIcon()} $message"
            if (storedData.isNotEmpty()) {
                result["Stored"] = storedData.mapKeys { it.key.toString() }.mapValues { it.value.toString() }
            }
            if (nodes.isNotEmpty()) {
                result["Expectations"] = nodes.map { it.toMap() }
            }
            return result
        }

        return DEBUGGER_MAPPER.writeValueAsString(root.toMap())
    }
}
