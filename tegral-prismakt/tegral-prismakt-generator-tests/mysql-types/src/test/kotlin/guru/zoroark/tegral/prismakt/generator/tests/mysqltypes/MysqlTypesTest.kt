package guru.zoroark.tegral.prismakt.generator.tests.mysqltypes

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import prismakt.generated.MysqlTypesEntity
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Testcontainers
class MysqlTypesTest {
    @Container
    val mysql = GenericContainer("mysql:8.0").apply {
        withExposedPorts(3306)
        withEnv("MYSQL_ROOT_PASSWORD", "root")
        withEnv("MYSQL_DATABASE", "db")
    }

    lateinit var db: Database

    @BeforeEach
    fun initmysql() {
        val connectionUrl = "mysql://root:root@${mysql.host}:${mysql.firstMappedPort}/db"
        val res = ProcessBuilder()
            .apply {
                command("gradle", "prismaDbPush")
                environment()["DATABASE_URL"] = connectionUrl
                inheritIO()
            }
            .start()
            .waitFor()
        require(res == 0) { "'prisma db push' failed" }
        db = Database.connect(
            "jdbc:mysql://${mysql.host}:${mysql.firstMappedPort}/db?sessionVariables=sql_mode=ANSI_QUOTES",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "root"

        )
    }

    @Test
    // this is all in one function for performance reasons, each test recreates a db andre-calls prisma db push, which
    // takes a while.
    @Suppress("LongMethod")
    fun `Test write-read all type`() {
        transaction(db) {
            MysqlTypesEntity.new(19u) {
                string = "string"
                char = "char"
                varchar = "varchar"
                text = "text"
                tinyText = "tinyText"
                mediumText = "mediumText"
                longText = "longText"

                bool = true

                int = 1234567891
                uint = 1234567891u // TODO 4234567891u
                tinyInt = 123
                utinyInt = 123u // TODO 213u
                smallInt = 12345
                usmallInt = 12345u // TODO 42345u
                mediumInt = 123456
                umediumInt = 123456u // TODO 123456u

                bigint = 8234567891234567891
                ubigInt = 8234567891234567891u // TODO would use 9234567891234567891u but is broken :(
                // ends up sending a negative one probably because there's a ulong to long conversion somewhere
                // TODO cf https://stackoverflow.com/questions/38830257

                double = 1.23
                float = 1.23F

                decimal = BigDecimal("1.5")

                timestamp = Instant.ofEpochSecond(123)
                date = LocalDate.of(2000, 1, 1)
                time = LocalTime.of(10, 10)
                dateTime = LocalDateTime.of(2000, 1, 1, 10, 10)

                binary = byteArrayOf(1, 2, 3)
                varBinary = byteArrayOf(4, 5, 6)
                tinyBlob = byteArrayOf(7, 8, 9)
                blob = byteArrayOf(10, 11, 12)
                mediumBlob = byteArrayOf(13, 14, 15)
                longBlob = byteArrayOf(16, 17, 18)
            }
        }

        val entity = transaction(db) { MysqlTypesEntity.all().first() }
        assertEquals("string", entity.string)
        assertEquals("char", entity.char)
        assertEquals("varchar", entity.varchar)
        assertEquals("text", entity.text)
        assertEquals("tinyText", entity.tinyText)
        assertEquals("mediumText", entity.mediumText)
        assertEquals("longText", entity.longText)

        assertEquals(true, entity.bool)

        assertEquals(1234567891, entity.int)
        assertEquals(1234567891u, entity.uint)
        assertEquals(123, entity.tinyInt)
        assertEquals(123u, entity.utinyInt)
        assertEquals(12345, entity.smallInt)
        assertEquals(12345u, entity.usmallInt)
        assertEquals(123456, entity.mediumInt)
        assertEquals(123456u, entity.umediumInt)

        assertEquals(8234567891234567891, entity.bigint)
        assertEquals(8234567891234567891u, entity.ubigInt)
        assertEquals(19u, entity.bigSerial)
        assertEquals(19u, entity.id.value)

        assertEquals(1.23, entity.double)
        assertEquals(1.23F, entity.float)

        assertTrue(entity.decimal.compareTo(BigDecimal("1.5")) == 0)

        assertEquals(Instant.ofEpochSecond(123), entity.timestamp)
        assertEquals(LocalDate.of(2000, 1, 1), entity.date)
        assertEquals(LocalTime.of(10, 10), entity.time)
        assertEquals(LocalDateTime.of(2000, 1, 1, 10, 10), entity.dateTime)

        assertContentEquals(byteArrayOf(1, 2, 3, 0, 0, 0, 0, 0, 0, 0), entity.binary)
        assertContentEquals(byteArrayOf(4, 5, 6), entity.varBinary)
        assertContentEquals(byteArrayOf(7, 8, 9), entity.tinyBlob)
        assertContentEquals(byteArrayOf(10, 11, 12), entity.blob)
        assertContentEquals(byteArrayOf(13, 14, 15), entity.mediumBlob)
        assertContentEquals(byteArrayOf(16, 17, 18), entity.longBlob)
    }
}
