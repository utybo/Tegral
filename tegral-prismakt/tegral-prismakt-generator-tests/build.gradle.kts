plugins {
    id("tegral.coverage-aggregator")
}

dependencies {
    for (subproject in subprojects) {
        "aggregatedProjects"(project(subproject.name))
    }
    "aggregatedProjects"(project(":tegral-prismakt:tegral-prismakt-generator"))
}
