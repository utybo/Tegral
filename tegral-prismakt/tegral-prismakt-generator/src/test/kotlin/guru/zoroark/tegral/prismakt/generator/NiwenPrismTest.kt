package guru.zoroark.tegral.prismakt.generator

import guru.zoroark.tegral.prismakt.generator.parser.NiwenPrism
import org.intellij.lang.annotations.Language
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
        val tokens = NiwenPrism.lexer.tokenize(fullExample)
        println(tokens)
    }

}