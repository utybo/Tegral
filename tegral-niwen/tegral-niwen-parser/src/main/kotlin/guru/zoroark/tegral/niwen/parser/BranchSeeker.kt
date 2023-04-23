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


class BranchSeeker {
    enum class Status { SUCCESS, DID_NOT_MATCH }
    data class Node(
        val parent: Node?,
        var title: String,
        var status: Status? = null,
        var message: String? = null,
        val nodes: MutableList<Node> = mutableListOf(),
        val storedData: MutableMap<NodeParameterKey<*, *>, Any?> = mutableMapOf()
    ) {
        fun createChild(title: String): Node {
            val child = Node(this, title)
            nodes += child
            return child
        }
    }

    private val root = Node(null, "Root")
    private var current = root

    fun stepIn(nodeTitle: String) {
        current = current.createChild(nodeTitle)
    }

    fun stepOut(status: Status, message: String, storedData: Map<NodeParameterKey<*, *>, Any?>) {
        val parent = requireNotNull(current.parent)
        current.status = status
        current.message = message
        current.storedData.putAll(storedData)
        current = parent
    }

    fun updateRoot(status: Status, message: String, storedData: Map<NodeParameterKey<*, *>, Any?>) {
        root.status = status
        root.message = message
        root.storedData.putAll(storedData)
    }

    fun toYamlRepresentation(): String {
        fun Status?.toIcon(): String = when (this) {
            null -> "❔"
            Status.SUCCESS -> "✅"
            Status.DID_NOT_MATCH -> "❌"
        }

        fun Node.toMap(): LinkedHashMap<String, Any> {
            val result = linkedMapOf<String, Any>()
            result[title] = "${status.toIcon()} $message"
            if (storedData.isNotEmpty()) result["Stored"] =
                storedData.mapKeys { it.key.toString() }.mapValues { it.value.toString() }
            if (nodes.isNotEmpty()) result["Expectations"] = nodes.map { it.toMap() }
            return result
        }

        return DEBUGGER_MAPPER.writeValueAsString(root.toMap())
    }

}
