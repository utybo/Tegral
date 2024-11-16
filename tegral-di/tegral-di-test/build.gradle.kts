plugins {
    id("tegral.kotlin-published-library-conventions")
}

extra["includeInBundles"] = listOf("web-test")

dependencies {
    api(project(":tegral-di:tegral-di-core"))
    implementation(libs.kotlin.reflect)
    api(libs.kotlin.test)
    implementation(libs.kotlin.coroutines)

    testImplementation(libs.mockk)
}

extra["humanName"] = "Tegral DI Test"
extra["description"] = "Test utilities for Tegrarl DI-powered applications."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/di/testing/"
