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

package guru.zoroark.tegral.prismakt.generator.tests.pgsqltypes

import guru.zoroark.tegral.prismakt.generator.tests.prismaDbPush
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import prismakt.generated.PgsqlTypesEntity
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@Testcontainers
class PgsqlTypesTest {
    @Container
    val pgsql = GenericContainer("postgres:15.2-alpine").apply {
        withExposedPorts(5432)
        withEnv("POSTGRES_PASSWORD", "postgres")
    }

    lateinit var db: Database

    @BeforeEach
    fun initPgsql() {
        prismaDbPush("postgresql://postgres:postgres@${pgsql.host}:${pgsql.firstMappedPort}/postgres")
        db = Database.connect(
            "jdbc:postgresql://${pgsql.host}:${pgsql.firstMappedPort}/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )
    }

    @Test
    fun `Test write-read all type`() {
        val uuid = UUID.randomUUID()
        transaction(db) {
            PgsqlTypesEntity.new(uuid) {
                text = "text"
                char = "char"
                varchar = "varchar"
                this.uuid = uuid
                boolean = true
                int = 12345678
                smallInt = 12345
                oid = 123
                bigint = 12345678910
                double = 1.5
                real = 1.5F
                decimal = BigDecimal("1.5")
                timestamp = Instant.ofEpochSecond(0)
                timestamptz = Instant.ofEpochSecond(10_000_000)
                date = LocalDate.of(2000, 1, 1)
                time = LocalTime.of(10, 10)
                timetz = LocalTime.of(10, 10)
                bytea = byteArrayOf(11, 22, 33)
            }
        }

        transaction(db) {
            val entity = PgsqlTypesEntity[uuid]
            assertEquals("text", entity.text)
            assertEquals("char                ", entity.char)
            assertEquals("varchar", entity.varchar)
            assertEquals(uuid, entity.uuid)
            assertEquals(true, entity.boolean)
            assertEquals(12345678, entity.int)
            assertEquals(12345, entity.smallInt)
            assertNotEquals(0, entity.smallSerial)
            assertNotEquals(0, entity.serial)
            assertEquals(123, entity.oid)
            assertEquals(12345678910, entity.bigint)
            assertNotEquals(0, entity.bigSerial)
            assertEquals(1.5, entity.double)
            assertEquals(1.5F, entity.real)
            assertEquals(0, BigDecimal("1.5").compareTo(entity.decimal))
            assertEquals(Instant.ofEpochSecond(0), entity.timestamp)
            assertEquals(Instant.ofEpochSecond(10_000_000), entity.timestamptz)
            assertEquals(LocalDate.of(2000, 1, 1), entity.date)
            assertEquals(LocalTime.of(10, 10), entity.time)
            assertEquals(LocalTime.of(10, 10), entity.timetz)
            assertContentEquals(byteArrayOf(11, 22, 33), entity.bytea)
        }
    }
}
