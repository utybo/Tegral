plugins {
    id("tegral.pktg-e2e-test")
}

dependencies {
    generator(project(":tegral-prismakt:tegral-prismakt-generator"))

    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.sqlDrivers.postgresql)
}
