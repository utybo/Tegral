package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.servers.Server

interface ServersDsl {
    operator fun String.invoke(builder: ServerDsl.() -> Unit)
}
class ServersBuilder : ServersDsl, Builder<List<Server>> {
    private val servers = mutableListOf<Builder<Server>>()

    override fun build(): List<Server> {
        return servers.map { it.build() }
    }

    override fun String.invoke(builder: ServerDsl.() -> Unit) {
        val result = ServerBuilder(this).apply(builder)
        servers.add(result)
    }
}

interface ServerDsl
class ServerBuilder(private val url: String) : ServerDsl, Builder<Server> {
    // TODO missing properties here
    override fun build() = Server().apply {
        url(this@ServerBuilder.url)
    }
}
