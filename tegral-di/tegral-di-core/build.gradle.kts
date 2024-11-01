plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-core"))
    implementation(libs.kotlin.reflect)

    testImplementation(project(":tegral-di:tegral-di-test"))
}

extra["humanName"] = "Tegral DI Core"
extra["description"] = "Main Tegral DI module for use in main source code -- dependency injection framework for Kotlin."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/di"
