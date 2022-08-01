package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.Paths

/**
 * DSL for the [paths object](https://spec.openapis.org/oas/v3.1.0#paths-object).
 */
interface PathsDsl {
    /**
     * Adds a path with the given string and registers any operations defined in the block.
     */
    operator fun String.invoke(path: PathDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "GET" operation on it.
     */
    infix fun String.get(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "POST" operation on it.
     */
    infix fun String.post(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "PUT" operation on it.
     */
    infix fun String.put(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "DELETE" operation on it.
     */
    infix fun String.delete(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "PATCH" operation on it.
     */
    infix fun String.patch(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "OPTIONS" operation on it.
     */
    infix fun String.options(path: OperationDsl.() -> Unit)

    /**
     * Adds a path with the given string and creates a "HEAD" operation on it.
     */
    infix fun String.head(path: OperationDsl.() -> Unit)
}

/**
 * Builder for [PathsDsl].
 */
class PathsBuilder(private val context: KoaDslContext) : PathsDsl, Builder<Paths> {
    private val pathBuilders = mutableMapOf<String, PathBuilder>()

    override fun String.invoke(path: PathDsl.() -> Unit) {
        pathBuilders.getOrPut(this) { PathBuilder(context) }.path()
    }

    override fun String.get(path: OperationDsl.() -> Unit) {
        this { get(path) }
    }

    override fun String.post(path: OperationDsl.() -> Unit) {
        this { post(path) }
    }

    override fun String.put(path: OperationDsl.() -> Unit) {
        this { put(path) }
    }

    override fun String.delete(path: OperationDsl.() -> Unit) {
        this { delete(path) }
    }

    override fun String.patch(path: OperationDsl.() -> Unit) {
        this { patch(path) }
    }

    override fun String.options(path: OperationDsl.() -> Unit) {
        this { options(path) }
    }

    override fun String.head(path: OperationDsl.() -> Unit) {
        this { head(path) }
    }

    override fun build(): Paths {
        val result = Paths()
        for ((key, builder) in pathBuilders) {
            val path = builder.build()
            result[key] = path
        }
        return result
    }

}
