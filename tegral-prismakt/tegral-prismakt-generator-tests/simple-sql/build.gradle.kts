plugins {
    id("tegral.pktg-e2e-test")
}

dependencies {
    generator(project(":tegral-prismakt:tegral-prismakt-generator"))

    testImplementation(libs.commons.lang3)
}
