plugins {
    id("tegral.kotlin-published-library-conventions")
}

extra["humanName"] = "Tegral Core"
extra["description"] =
    "Contains a few common Tegral classes, annotations and interfaces required by all other Tegral modules"
extra["url"] = "https://tegral.zoroark.guru/docs/modules/core"

val generateKotlin = tasks.register<Copy>("generateKotlin") {
    val templateContext = mapOf("version" to project.version)
    inputs.properties(templateContext)
    from("src/template/kotlin")
    into("${layout.buildDirectory}/generated/kotlin")
    expand(templateContext)
}

sourceSets {
    main {
        kotlin {
            srcDir(layout.buildDirectory.dir("generated/kotlin"))
        }
    }
}

tasks.named("compileKotlin") { dependsOn(generateKotlin) }
tasks.named("checkLicenseMain") { dependsOn(generateKotlin) }
tasks.named("dokkatooGenerateModuleHtml") { dependsOn(generateKotlin) }
tasks.named("sourcesJar") { dependsOn(generateKotlin) }
