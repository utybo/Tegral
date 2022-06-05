package guru.zoroark.tegral.web.appdsl

import com.sksamuel.hoplite.sources.InputStreamPropertySource
import guru.zoroark.tegral.di.dsl.put
import guru.zoroark.tegral.web.controllers.KtorController
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.java.Java
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import kotlinx.coroutines.runBlocking
import org.intellij.lang.annotations.Language
import kotlin.test.Test
import kotlin.test.assertEquals

class FullApplicationTest {
    class HelloController : KtorController() {
        override fun Routing.install() {
            get("/") {
                call.respondText("Hello!")
            }
        }
    }

    @Test
    fun `Start, test and stop full application`() {
        @Language("toml")
        val config = """
            [tegral.web]
            port = 8970
        """.trimIndent()
        val app = tegral {
            put(::HelloController)
            useConfiguration {
                addSource(
                    InputStreamPropertySource(config.toByteArray().inputStream(), "toml", "<implicit>")
                )
            }
        }
        val client = HttpClient(Java)
        runBlocking {
            val result = client.get("http://localhost:8970")
            assertEquals("Hello!", result.bodyAsText())
            app.stop()
        }
    }
}
