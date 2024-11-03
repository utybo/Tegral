plugins {
    id("tegral.kotlin-common-conventions")
}

dependencies {
    testImplementation(project(":tegral-web:tegral-web-appdsl"))
    testImplementation(libs.ktor.client.java)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

