plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-core"))
}

extra["humanName"] = "Tegral Niwen Lexer"
extra["description"] = "Tegral Niwen Lexer is an easy-to-use lexer library for Kotlin"
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/niwen/lexer"
