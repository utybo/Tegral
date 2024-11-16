plugins {
    id("tegral.kotlin-published-library-conventions")
}

extra["includeInBundles"] = listOf("web-test")

dependencies {
    api(project(":tegral-web:tegral-web-controllers"))
    api(project(":tegral-di:tegral-di-test"))
    api(project(":tegral-web:tegral-web-appdefaults"))

    api(libs.ktor.server.test)
    api(libs.ktor.client.contentNegotiation)
}

extra["humanName"] = "Tegral Web Controllers Test"
extra["description"] = "Test classes for testing Tegral Web Controllers classes easily."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/web/controllers"
