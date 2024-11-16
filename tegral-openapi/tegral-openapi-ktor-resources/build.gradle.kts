plugins {
    id("tegral.kotlin-published-library-conventions")

    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    api(project(":tegral-openapi:tegral-openapi-dsl"))
    api(project(":tegral-openapi:tegral-openapi-ktor"))
    implementation(project(":tegral-core"))

    api(libs.ktor.server.resources)
    implementation(libs.kotlin.reflect)

    testImplementation(libs.ktor.server.test)
}

extra["humanName"] = "Tegral OpenAPI Ktor Resources"
extra["description"] = "Easily create OpenAPI documentation for your Ktor application using Tegral OpenApi Ktor alongisde the Resources plug-in!"
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/openapi/ktor"
