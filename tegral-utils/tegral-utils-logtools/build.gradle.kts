plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(libs.slf4j)
    api(libs.logback)
}

extra["humanName"] = "Tegral Utils: LogTools"
extra["description"] = "Various logging utilities for Logback"
extra["url"] = "https://tegral.zoroark.guru/docs/modules/utils/logtools" // TODO
