plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    implementation(project(":tegral-di:tegral-di-core"))
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.coroutines)
    api(project(":tegral-services:tegral-services-api"))
}

extra["humanName"] = "Tegral DI Services"
extra["description"] = "Extension for Tegral DI that provides necessary tooling and hooks for integrating Tegral Services in a Tegral DI environment."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/di/extensions/services"
