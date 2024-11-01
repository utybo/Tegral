plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-config:tegral-config-core"))
}

ext["humanName"] = "Tegral Web Config"
ext["description"] = "Provides the necessary configuration sections for Tegral Web apps."
ext["url"] = "https://tegral.zoroark.guru/docs/modules/web/config"
