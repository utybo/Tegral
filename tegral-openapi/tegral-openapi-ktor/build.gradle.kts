plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-openapi:tegral-openapi-dsl"))
    implementation(project(":tegral-core"))

    api(libs.ktor.server.core)

    testImplementation(libs.ktor.server.test)
}

extra["humanName"] = "Tegral OpenAPI Ktor"
extra["description"] = "Easily create OpenAPI documentation for your Ktor application using Tegral OpenApi Ktor!"
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/openapi/ktor"
