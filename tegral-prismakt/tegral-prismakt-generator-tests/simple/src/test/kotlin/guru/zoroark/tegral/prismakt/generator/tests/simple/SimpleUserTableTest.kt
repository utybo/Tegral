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
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import prismakt.generated.UserEntity
import prismakt.generated.UserTable
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
            UserTable.deleteAll()
        }
        block(db)
    }
}

class SimpleUserTableTest {
    @Test
    fun `Simple database transaction with DSL API`() = withDb {
        val id = newSuspendedTransaction(db = this) {
            UserTable.insertAndGetId {
                it[email] = "user@example.com"
                it[name] = "User"
            }
        }

        val result = newSuspendedTransaction {
            UserTable.select(UserTable.id eq id).single()
        }

        assertEquals("user@example.com", result[UserTable.email])
        assertEquals("User", result[UserTable.name])
    }

    @Test
    fun `Simple database transactino with DAO API`() = withDb {
        val id = newSuspendedTransaction(db = this) {
            UserEntity.new {
                email = "user2@example.com"
                name = "User2"
            }.id
        }

        val result = newSuspendedTransaction {
            UserEntity.findById(id)
        }

        assertNotNull(result)
        assertEquals("user2@example.com", result.email)
        assertEquals("User2", result.name)
    }
}