plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-di:tegral-di-core"))
    api(project(":tegral-config:tegral-config-core"))
}

extra["humanName"] = "Tegral Featureful"
extra["description"] = "Contains common classes for writing Tegral features that will get integrated in main applications."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/featureful"
