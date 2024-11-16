plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-di:tegral-di-services"))
    api(project(":tegral-featureful"))
}

extra["humanName"] = "Tegral Services Feature"
extra["description"] = "Tegral feature used to load, start and manage Tegral services."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/services/"
