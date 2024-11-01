plugins {
    id("tegral.kotlin-published-library-conventions")
}

extra["includeInBundles"] = listOf("web-test")

dependencies {
    api(project(":tegral-di:tegral-di-test"))
    api(project(":tegral-web:tegral-web-appdsl"))
    api(project(":tegral-web:tegral-web-controllers-test"))
}

extra["humanName"] = "Tegral Web AppTest"
extra["description"] = "Integration testing utilities for Tegral applications."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/web/apptest"
