plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-di:tegral-di-core"))
    api(project(":tegral-featureful"))
    api(project(":tegral-services:tegral-services-feature"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.host)
    api(libs.ktor.server.contentNegotiation)
    api(libs.ktor.serialization.jackson)

    testImplementation(libs.ktor.server.netty)
    testImplementation(libs.ktor.client.java)
    testImplementation(libs.ktor.client.contentNegotiation)
    testImplementation(libs.logback)
}

extra["humanName"] = "Tegral Web Controllers"
extra["description"] = "Provides necessary classes to integrate the Ktor framework into a Tegral Web application."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/web/controllers"
