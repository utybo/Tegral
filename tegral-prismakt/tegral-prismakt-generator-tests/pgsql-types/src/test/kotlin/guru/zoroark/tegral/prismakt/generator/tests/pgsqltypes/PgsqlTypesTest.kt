package guru.zoroark.tegral.prismakt.generator.tests.pgsqltypes

import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class PgsqlTypesTest {
    val port = 28080
    @Container
    val pgsql = GenericContainer("postgres:15.2-alpine").withExposedPorts(port)

}
