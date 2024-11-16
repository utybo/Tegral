plugins {
    id("tegral.kotlin-published-library-conventions")
}

extra["includeInBundles"] = listOf("web")

dependencies {
    api(project(":tegral-di:tegral-di-core"))
    api(project(":tegral-config:tegral-config-core"))
    api(project(":tegral-featureful"))
    api(project(":tegral-web:tegral-web-controllers"))
    api(project(":tegral-web:tegral-web-appdefaults"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)

    testImplementation(libs.ktor.client.java)
}

extra["humanName"] = "Tegral Web AppDSL"
extra["description"] = "Contains common classes for writing Tegral features that will get integrated in main applications."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/web/appdsl"
