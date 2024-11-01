plugins {
    id("tegral.kotlin-published-library-conventions")
}

ext["includeInBundles"] = listOf("web-test")

dependencies {
    api(project(":tegral-di:tegral-di-core"))
    implementation(libs.kotlin.reflect)
    api(libs.kotlin.test)
    implementation(libs.kotlin.coroutines)

    testImplementation(libs.mockk)
}

ext["humanName"] = "Tegral DI Test"
ext["description"] = "Test utilities for Tegrarl DI-powered applications."
ext["url"] = "https://tegral.zoroark.guru/docs/modules/core/di/testing/"
