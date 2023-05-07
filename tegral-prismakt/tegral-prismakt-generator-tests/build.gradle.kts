plugins {
    id("guru.zoroark.tegral.coverage-aggregator")
}

dependencies {
    for (subproject in subprojects) {
        "aggregatedProjects"(subproject)
    }
    "aggregatedProjects"(project(":tegral-prismakt:tegral-prismakt-generator"))
}
