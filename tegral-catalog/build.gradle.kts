plugins {
    id("version-catalog")
    id("maven-publish")

    id("tegral.base-conventions")
    id("tegral.publish-conventions")
}

// FIXME better typing
fun computeLibrariesToInclude(): Pair<List<Pair<String, String>>, Map<String, List<String>>> {
    val result = mutableListOf<Pair<String, String>>()
    val bundles = mutableMapOf<String, MutableList<String>>()

    val projects = rootProject.getSubprojects()
    for (project in projects) {
        val includeInCatalog = project.extensions.extraProperties.properties["includeInCatalog"] as? Boolean ?: false
        if (includeInCatalog) {
            val name = project.getName().let { if (it.startsWith("tegral-")) it.substring(7) else it }

            result.add(name to project.group.toString() + ':' + project.name.toString() + ':' + project.version.toString())

            val relevantBundles = project.extensions.extraProperties.properties["includeInBundles"] as? List<*> ?: listOf<Any>()
            for (bundle in relevantBundles) {
                if (!bundles.containsKey(bundle)) {
                    bundles[bundle.toString()] = mutableListOf()
                }
                bundles.get(bundle)!!.add(name)
            }
        }
    }
    return result to bundles
}

// FIXME: This doesn't look right
val setupVersionCatalog = tasks.register("setupVersionCatalog") {
    doLast {
        catalog {
            versionCatalog {
                val (exportedLibraries, exportedBundles) = computeLibrariesToInclude()
                for (exportedLibrary in exportedLibraries) {
                    val (name, dependency) = exportedLibrary
                    library(name, dependency)
                }

                print(exportedBundles)

                for ((bundleName, bundleLibraries) in exportedBundles.entries) {
                    bundle(bundleName, bundleLibraries)
                }
            }
        }
    }
}

tasks.named("generateCatalogAsToml") { dependsOn(setupVersionCatalog) }

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
