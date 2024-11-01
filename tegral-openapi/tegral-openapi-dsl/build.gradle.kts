plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(libs.swaggerCore.models)
    api(libs.swaggerCore.core)
    implementation(project(":tegral-core"))
}

extra["humanName"] = "Tegral OpenAPI DSL"
extra["description"] = "Provides a DSL for creating OpenAPI 3 documents."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/openapi/dsl"

license {
    exclude("petstore-simple.openapi.yaml")
}
