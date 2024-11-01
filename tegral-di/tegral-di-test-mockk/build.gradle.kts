plugins {
    id("tegral.kotlin-published-library-conventions")
}
extra["includeInBundles"] = listOf("web-test")

dependencies {
    api(project(":tegral-di:tegral-di-test"))
    api(libs.mockk)
}

extra["humanName"] = "Tegral DI Test MockK"
extra["description"] = "Provides integration between Tegral DI and MockK for test code."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/di/testing"
