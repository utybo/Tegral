plugins {
    id("version-catalog")
    id("maven-publish")

    id("tegral.base-conventions")
    id("tegral.publish-conventions")
}

data class LibEntry(val name: String, val artifact: String)
data class ComputedEntries(val libs: MutableList<LibEntry>, val bundles: MutableMap<String, MutableList<String>>)

fun computeLibrariesToInclude(): ComputedEntries {
    val computedEntries = ComputedEntries(mutableListOf(), mutableMapOf())

    for (project in rootProject.subprojects) {
        val includeInCatalog = project.extra.properties["includeInCatalog"] as? Boolean ?: false
        if (includeInCatalog) {
            val name = project.name.let { if (it.startsWith("tegral-")) it.substring(7) else it }

            computedEntries.libs.add(
                LibEntry(
                    name,
                    project.group.toString() + ':' + project.name + ':' + project.version.toString()
                )
            )

            val relevantBundles =
                project.extra.properties["includeInBundles"] as? List<*> ?: listOf<Any>()
            for (bundle in relevantBundles) {
                computedEntries.bundles.computeIfAbsent(bundle.toString()) { mutableListOf() }.add(name)
            }
        }
    }
    return computedEntries
}

gradle.projectsEvaluated {
    catalog {
        versionCatalog {
            val (exportedLibraries, exportedBundles) = computeLibrariesToInclude()
            for ((name, dependency) in exportedLibraries) {
                library(name, dependency)
            }

            for ((bundleName, bundleLibraries) in exportedBundles.entries) {
                bundle(bundleName, bundleLibraries)
            }
        }
    }
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            from(components["versionCatalog"])
        }
    }
}

extra["humanName"] = "Tegral Catalog"
extra["description"] = "Gradle Versions Catalog that makes it easy to get dependencies from the Tegral project"
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core/catalog/"
