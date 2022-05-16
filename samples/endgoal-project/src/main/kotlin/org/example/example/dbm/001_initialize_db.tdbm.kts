info {
    name = "Alter foobar, tweet authors and add cookies"
}

transaction {
    // This block contains regular Exposed code with additional table-operation
    // syntaxes for easily modifying tables.


    // 1. MODIFICATION
    // Declare only the parts of your models that change + any additional parts
    // you need to make SQL transactions

    // For example, we're adding a foo column and removing a bar column
    // (UUIDTableDiff, alter add, alter remove from TDBM, varchar and basic table stuff from Exposed)
    object Users : UUIDTableDiff("users") {
        val foo by alter add varchar("foo", 50)
        val bar by alter remove varchar("bar", 256)
    }

    alterTable(Users)

    // More complex case where we first alter with a nullable field, add proper references, then set the field to nullable

    object TweetWithNullableUsername : TableDiff("users") {
        val usernameNullable by altering add optionalReference("author", Users)
    }

    object FinalTweet : TableDiff("users") {
        val username by altering type reference("author", Users)
    }

    alterTable(TweetWithNullableUsername)

    // A regular Exposed DSL call (for once...)
    TweetWithNullableUsername.update {
        it[TweetWithNullableUsername.usernameNullable] =
            Users.findFirst()[Users.id]
    }

    alterTable(FinalTweet)

    // 2. ADDING A TABLE
    // Simpler, just put the entire Exposed model here
    // (no need to put TableDiff since we aren't diffing tables)
    object Cookies : UUIDTable("cookies") {
        val name = varchar("name", 256).uniqueIndex()
        val value = varchar("value", 5000)
    }

    addTable("cookies")

    // 3. DROPPING A TABLE
    // You don't even need to specify the table
    dropTable("old_stuff")
}
