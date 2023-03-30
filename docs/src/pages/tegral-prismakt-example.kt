// schema.prisma
model Person {
    id   Int
    name String
}

// prismakt/generated/MyTable.kt
public object PersonTable : IdTable<Int>(name = "Person") {
    public override val id = integer("id").entityId()
    public val name = varchar("name")
}
