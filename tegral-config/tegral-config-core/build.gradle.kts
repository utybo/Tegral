plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(libs.hoplite.toml)
    api(libs.hoplite.yaml)
    api(libs.hoplite.json)
    implementation(libs.jackson.dataformats.toml)

    implementation(project(":tegral-core"))

    testImplementation(libs.jimfs)
}

extra["humanName"] = "Tegral Config Core"
extra["description"] = "Provides core configuration mechanisms with Hoplite"
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/config"
