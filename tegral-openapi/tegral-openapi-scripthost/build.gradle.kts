plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-core"))
    api(project(":tegral-openapi:tegral-openapi-dsl"))
    api(project(":tegral-openapi:tegral-openapi-scriptdef"))
    api(libs.kotlin.scripting.common)
    api(libs.kotlin.scripting.jvm)
    api(libs.kotlin.scripting.jvmHost)

    testImplementation(libs.jimfs)
    testImplementation(libs.kotlin.coroutines)
}

extra["humanName"] = "Tegral OpenAPI Script (host)"
extra["description"] = "Host for Tegral OpenAPI scripts. Allows you to compilet .openapi.kts files right from your application."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/openapi/scripting"
