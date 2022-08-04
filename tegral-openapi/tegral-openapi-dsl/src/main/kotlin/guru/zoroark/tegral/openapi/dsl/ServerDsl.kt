package guru.zoroark.tegral.openapi.dsl

import io.swagger.v3.oas.models.servers.Server

interface ServerDsl
class ServerBuilder(private val url: String) : ServerDsl, Builder<Server> {
    // TODO missing properties here
    override fun build() = Server().apply {
        url(this@ServerBuilder.url)
    }
}
