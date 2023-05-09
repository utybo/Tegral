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

package guru.zoroark.tegral.prismakt.generator.tests.simple

import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.junit.jupiter.api.BeforeEach
import prismakt.generated.DaoUserEntity
import prismakt.generated.DaoUserTable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

object TableThing : IntIdTable() {
    val bruh = bool("bruh")
    val bruhh = long("bruhh")
    val bruhhh = datetime("bruhhh")
}

fun withDb(block: suspend Database.() -> Unit) {
    val db = Database.connect("jdbc:sqlite:prisma/dev.db", "org.sqlite.JDBC")

    runBlocking {
        newSuspendedTransaction(db = db) {
            DaoUserTable.deleteAll()
        }
        block(db)
    }
}

class SimpleUserTableTest {
    @BeforeEach
    fun resetDb() {
        val res = ProcessBuilder()
            .apply {
                command("gradle", "prismaDbPush")
                inheritIO()
            }
            .start()
            .waitFor()
        require(res == 0) { "'prisma db push' failed" }
    }

    @Test
    fun `Simple database transaction with DSL API`() = withDb {
        val id = newSuspendedTransaction(db = this) {
            DaoUserTable.insertAndGetId {
                it[email] = "user@example.com"
                it[name] = "User"
            }
        }

        val result = newSuspendedTransaction {
            DaoUserTable.select(DaoUserTable.id eq id).single()
        }

        assertEquals("user@example.com", result[DaoUserTable.email])
        assertEquals("User", result[DaoUserTable.name])
    }

    @Test
    fun `Simple database transaction with DAO API`() = withDb {
        val id = newSuspendedTransaction(db = this) {
            DaoUserEntity.new {
                email = "user2@example.com"
                name = "User2"
            }.id
        }

        val result = newSuspendedTransaction {
            DaoUserEntity.findById(id)
        }

        assertNotNull(result)
        assertEquals("user2@example.com", result.email)
        assertEquals("User2", result.name)
    }
}
