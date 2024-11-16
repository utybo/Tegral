plugins {
    id("tegral.kotlin-published-library-conventions")
    id("application")
}

dependencies {
    implementation(project(":tegral-openapi:tegral-openapi-scripthost"))
    implementation(project(":tegral-utils:tegral-utils-logtools"))
    implementation(libs.clikt)
    implementation(libs.kotlin.coroutines)
    implementation(libs.logback)

    testImplementation(libs.jimfs)
}

application {
    mainClass = "guru.zoroark.tegral.openapi.cli.MainKt"
}

extra["humanName"] = "Tegral OpenAPI CLI"
extra["description"] = "A handy companion for working with .openapi.kts files."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/openapi/cli"

tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = "guru.zoroark.tegral.openapi.cli.MainKt"
    }
}
