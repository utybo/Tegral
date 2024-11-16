plugins {
    id("tegral.kotlin-published-library-conventions")
}

dependencies {
    api(project(":tegral-di:tegral-di-core"))
    api(project(":tegral-web:tegral-web-controllers"))
    api(project(":tegral-web:tegral-web-config"))
    api(project(":tegral-web:tegral-web-greeter"))
    api(project(":tegral-logging"))
    implementation(libs.jackson.time)
    implementation(libs.ktor.server.netty)

    testImplementation(project(":tegral-di:tegral-di-test"))
    testImplementation(project(":tegral-di:tegral-di-test-mockk"))
}

extra["humanName"] = "Tegral Web AppDefaults"
extra["description"] = "Provides basic versions of all necessary classes in a Tegral Web environment."
extra["url"] = "https://tegral.zoroark.guru/docs/modules/web/appdefaults"
