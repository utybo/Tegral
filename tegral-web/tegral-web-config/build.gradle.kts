plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-config:tegral-config-core"))
}

extra["humanName"] = "Tegral Web Config"
extra["description"] = "Provides the necessary configuration sections for Tegral Web apps."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/web/config"
