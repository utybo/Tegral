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

package guru.zoroark.tegral.prismakt.generator

import guru.zoroark.tegral.prismakt.generator.parser.NiwenPrism
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.system.measureTimeMillis
import kotlin.test.Test

@Language("prisma")
val fullExample = """
datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}

generator client {
  provider = "prisma-client-js"
}

model User {
  id        Int      @id @default(autoincrement())
  createdAt DateTime @default(now())
  email     String   @unique
  name      String?
  role      Role     @default(USER)
  posts     Post[]
}

model Post {
  id        Int      @id @default(autoincrement())
  createdAt DateTime @default(now())
  updatedAt DateTime @updatedAt
  published Boolean  @default(false)
  title     String   @db.VarChar(255)
  author    User?    @relation(fields: [authorId], references: [id])
  authorId  Int?
}

enum Role { 
  USER 
  ADMIN 
}
"""

class NiwenPrismTest {
    @Test
    fun `Test full example`() {
        fun noDebug() {
            val tokens = NiwenPrism.tokenize(fullExample)
            NiwenPrism.parse(tokens)
        }

        fun withDebug() {
            val tokens = NiwenPrism.tokenize(fullExample)
            NiwenPrism.parseDebug(tokens)
        }

        assertDoesNotThrow {
            val timeTakenNoDebug = mutableListOf<Long>()
            val timeTakenDebug = mutableListOf<Long>()

            // Warmup
            repeat(10) {
                println("No debug: warmup $it")
                noDebug()
            }

            // Exec
            repeat(10) {
                print("No debug: exec $it | ")
                timeTakenNoDebug += measureTimeMillis(::noDebug)
                println("${timeTakenNoDebug.last()} ms")
            }

            println("No debug: avg ${timeTakenNoDebug.average()} ms")

            // Warmup
            repeat(10) { withDebug() }

            // Exec
            repeat(10) {
                print("W/ debug: exec $it | ")
                timeTakenDebug += measureTimeMillis(::withDebug)
                println("${timeTakenDebug.last()} ms")
            }

            println("W/ debug: avg ${timeTakenDebug.average()} ms")
        }
    }
}
