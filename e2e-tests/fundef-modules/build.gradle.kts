plugins {
    id("tegral.kotlin-common-conventions")
    id("application")
}

dependencies {
    implementation(project(":tegral-web:tegral-web-appdsl"))
    implementation(project(":tegral-openapi:tegral-openapi-feature"))

    testImplementation(project(":tegral-di:tegral-di-test"))
    testImplementation(project(":tegral-web:tegral-web-apptest"))
    testImplementation(project(":tegral-di:tegral-di-test-mockk"))
}
