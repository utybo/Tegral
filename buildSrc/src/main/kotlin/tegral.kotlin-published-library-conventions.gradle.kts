plugins {
    id("tegral.kotlin-common-conventions")
    id("tegral.publish-conventions")
    id("dev.adamko.dokkatoo")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")
    id("maven-publish")
    id("signing")
}

project.extra["includeInCatalog"] = true

detekt {
    config.from(rootProject.layout.projectDirectory.file("detekt-config.published.yaml"))
    buildUponDefaultConfig = true
}

dokkatoo {
    dokkatooSourceSets.configureEach {
        includes.from("MODULE.md")
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}


// https://docs.gradle.org/current/userguide/jacoco_plugin.html#sec:jacoco_getting_started
val test = tasks.named<Test>("test") {
    finalizedBy(tasks.named<JacocoReport>("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(test)
    reports {
        xml.required = true
        html.required = true
    }
}
