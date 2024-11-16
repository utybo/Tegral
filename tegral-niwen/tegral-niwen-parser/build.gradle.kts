plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-core"))
    api(project(":tegral-niwen:tegral-niwen-lexer"))
    api(libs.kotlin.reflect)

    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.dataformats.yaml)
}

extra["humanName"] = "Tegral Niwen Parser"
extra["description"] = "Tegral Niwen Parser is an easy-to-use parser library for Kotlin"
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/niwen/parser"
