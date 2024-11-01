plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    implementation(project(":tegral-featureful"))
    implementation(project(":tegral-logging"))
}

extra["humanName"] = "Tegral Web Greeter"
extra["description"] = "A simple feature that displays a message on startup."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/web/greeter"
