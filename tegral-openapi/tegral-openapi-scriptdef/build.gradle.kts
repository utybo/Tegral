plugins {
    id ("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-openapi:tegral-openapi-dsl"))
    implementation(libs.kotlin.scripting.common)
    implementation(libs.kotlin.scripting.jvm)
}

extra["humanName"] = "Tegral OpenAPI Script (definitions)"
extra["description"] = "Kotlin Scripting definitions for Tegral OpenAPI files (.openapi.kts)."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/openapi/scripting"
