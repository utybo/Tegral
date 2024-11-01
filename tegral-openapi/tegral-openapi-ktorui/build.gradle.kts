plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    implementation(libs.swaggerUi)
    implementation(libs.ktor.server.core)

    testImplementation(libs.ktor.server.test)
}

extra["humanName"] = "Tegral OpenAPI Ktor UI"
extra["description"] = "Integrates Swagger UI with Ktor."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/openapi/ktor"
