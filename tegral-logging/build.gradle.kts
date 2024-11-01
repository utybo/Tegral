plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-di:tegral-di-core"))
    api(project(":tegral-featureful"))

    api(libs.slf4j)
    api(libs.logback)
    implementation(libs.kotlin.reflect)

    testImplementation(libs.jimfs)
    testImplementation(project(":tegral-web:tegral-web-appdsl"))
}

extra["humanName"] = "Tegral Logging"
extra["description"] = "Logging feature for Tegral apps that makes it easy to create and manage loggers."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/logging/"
