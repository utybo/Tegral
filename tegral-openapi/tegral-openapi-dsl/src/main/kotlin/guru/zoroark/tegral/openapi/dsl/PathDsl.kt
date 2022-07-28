package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem

interface PathDsl {
    fun get(block: OperationDsl.() -> Unit)
    fun post(block: OperationDsl.() -> Unit)
    fun put(block: OperationDsl.() -> Unit)
    fun delete(block: OperationDsl.() -> Unit)
    fun patch(block: OperationDsl.() -> Unit)
    fun options(block: OperationDsl.() -> Unit)
    fun head(block: OperationDsl.() -> Unit)
}

class PathBuilder(private val context: KoaDslContext) : PathDsl, Builder<PathItem> {
    // TODO summary, description, ...

    private var get: Builder<Operation>? = null
    private var post: Builder<Operation>? = null
    private var put: Builder<Operation>? = null
    private var delete: Builder<Operation>? = null
    private var patch: Builder<Operation>? = null
    private var options: Builder<Operation>? = null
    private var head: Builder<Operation>? = null

    override fun get(block: OperationDsl.() -> Unit) {
        get = OperationBuilder(context).apply(block)
    }

    override fun post(block: OperationDsl.() -> Unit) {
        post = OperationBuilder(context).apply(block)
    }

    override fun put(block: OperationDsl.() -> Unit) {
        put = OperationBuilder(context).apply(block)
    }

    override fun delete(block: OperationDsl.() -> Unit) {
        delete = OperationBuilder(context).apply(block)
    }

    override fun patch(block: OperationDsl.() -> Unit) {
        patch = OperationBuilder(context).apply(block)
    }

    override fun options(block: OperationDsl.() -> Unit) {
        options = OperationBuilder(context).apply(block)
    }

    override fun head(block: OperationDsl.() -> Unit) {
        head = OperationBuilder(context).apply(block)
    }

    override fun build(): PathItem {
        return PathItem().apply {
            get = this@PathBuilder.get?.build()
            post = this@PathBuilder.post?.build()
            put = this@PathBuilder.put?.build()
            delete = this@PathBuilder.delete?.build()
            patch = this@PathBuilder.patch?.build()
            options = this@PathBuilder.options?.build()
            head = this@PathBuilder.head?.build()
        }
    }
}
