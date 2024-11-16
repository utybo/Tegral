plugins {
    id("base")
    id("tegral.coverage-aggregator")
}

repositories {
    mavenCentral()
}

dependencies {
    for (projectPath in gradle.extra["testProjects"] as List<*>) {
        aggregatedProjects(project(projectPath.toString()))
    }
}

tasks.named("check") {
    dependsOn(tasks["aggregatedCodeCoverage"])
}

