import dev.adamko.dokkatoo.tasks.DokkatooGenerateModuleTask
import dev.adamko.dokkatoo.tasks.DokkatooGeneratePublicationTask

plugins {
    id("dev.adamko.dokkatoo")
}

repositories {
    mavenCentral()
}

val dokkaHtml by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}

dokkatoo {
    moduleName = "Tegral API reference"
}

version = rootProject.version

dependencies {
    for (projectPath in gradle.extra["publicProjects"] as List<*>) {
        dokkatoo(project(projectPath.toString()))
    }
}

artifacts {
    add("dokkaHtml", tasks.named<DokkatooGeneratePublicationTask>("dokkatooGeneratePublicationHtml").map { it.outputDirectory }) {
        builtBy(tasks.named<DokkatooGeneratePublicationTask>("dokkatooGeneratePublicationHtml"))
    }
}