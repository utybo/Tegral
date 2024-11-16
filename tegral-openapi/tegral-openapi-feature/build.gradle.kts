plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-openapi:tegral-openapi-dsl"))
    api(project(":tegral-openapi:tegral-openapi-ktor"))
    api(project(":tegral-openapi:tegral-openapi-ktorui"))
    implementation(project(":tegral-logging"))
    implementation(project(":tegral-web:tegral-web-controllers"))
    implementation(project(":tegral-featureful"))

    testImplementation(libs.ktor.server.test)
}

extra["humanName"] = "Tegral OpenAPI Feature"
extra["description"] = "Tegral Web feature for OpenAPI integration into your Tegral applications"
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/openapi/tegral-web"
