plugins {
    id("tegral.kotlin-common-conventions")
    id("application")
}

dependencies {
    implementation(project(":tegral-core"))
    implementation(project(":tegral-web:tegral-web-appdsl"))
    implementation(project(":tegral-openapi:tegral-openapi-feature"))

    testImplementation(project(":tegral-di:tegral-di-test"))
    testImplementation(project(":tegral-di:tegral-di-test-mockk"))
    testImplementation(project(":tegral-web:tegral-web-controllers-test"))
    testImplementation(project(":tegral-web:tegral-web-apptest"))
}

application {
    mainClass = "guru.zoroark.tegral.examples.simplejsonapi.AppKt"
}
