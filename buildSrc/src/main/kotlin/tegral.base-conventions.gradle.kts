import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.github.ben-manes.versions")
    id("dev.yumi.gradle.licenser")
}

version = rootProject.version
group = rootProject.group

// Versions config for the Gradle versions plugin, from the readme
// https://github.com/ben-manes/gradle-versions-plugin

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = Regex("^[0-9,.v-]+(-r)?$")
    return !stableKeyword && !(version.matches(regex)) || version.contains("-M")
}


tasks.named<DependencyUpdatesTask>("dependencyUpdates").configure {
    // Reject all non-stable versions
    rejectVersionIf {
        isNonStable(candidate.version)
    }
}

license {
    rule(rootProject.file("LICENSE_HEADER"))
}
