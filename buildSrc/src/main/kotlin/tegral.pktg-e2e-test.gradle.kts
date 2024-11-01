import com.github.gradle.node.npm.task.NpxTask
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("tegral.kotlin-common-conventions")
    id("com.github.node-gradle.node")
    id("jacoco")
    id("project-report")
}

val libs = the<LibrariesForLibs>()

val generator by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false

    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, Bundling.SHADOWED))
    }
}

dependencies {
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.javatime)
    implementation(libs.sqlite)

    testImplementation(project(":tegral-prismakt:tegral-prismakt-generator-tests-support"))

    generator(project(":tegral-prismakt:tegral-prismakt-generator"))
}

sourceSets {
    main {
        kotlin {
            srcDir(layout.buildDirectory.dir("prismaGeneratedSrc"))
        }
    }
}


val prismaGenerate = tasks.register<NpxTask>("prismaGenerate") {
    command = "prisma"
    args = listOf("generate")

    inputs.file("prisma/schema.prisma")
    outputs.dir(project.layout.buildDirectory.dir("prismakt-generator"))
    outputs.file(layout.buildDirectory.file("jacoco/generator.exec"))

    dependsOn(configurations["jacocoAgent"])
    dependsOn(generator)

    environment.putAll(provider {
        // Agent file retrieval based on JacocoAgentJar.getJar from the Gradle JaCoCo plugin
        val jacocoAgentFile =
            zipTree(configurations["jacocoAgent"].singleFile).find { it.name.equals("jacocoagent.jar") }
        val command = "java " +
            "-javaagent:" + jacocoAgentFile + "=destfile=" + layout.buildDirectory.file("jacoco/generator.exec")
            .get().asFile.absolutePath +
            " -jar " + generator.find { it.name.endsWith(".jar") }
        logger.info("Generated KT command: $command")
        mapOf("PRISMAKT_CMD" to command)
    })
}

val generatorCodeCoverage = tasks.register<JacocoReport>("generatorCodeCoverage") {
    executionData.setFrom(layout.buildDirectory.file("jacoco/generator.exec"))
    sourceSets(sourceSets["main"])

    dependsOn(prismaGenerate)
}

val generatorCodeCoverageOutput by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false

    extendsFrom(configurations["coverageDataElementsForTest"])

    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named<Category>(Category.VERIFICATION))
        attribute(
            VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
            objects.named<VerificationType>(VerificationType.JACOCO_RESULTS)
        )
    }
}

artifacts {
    add(generatorCodeCoverageOutput.name, layout.buildDirectory.file("jacoco/generator.exec")) {
        builtBy(prismaGenerate)
    }
}

tasks.named<Task>("compileKotlin") { dependsOn(prismaGenerate) }
tasks.named<Task>("compileTestKotlin") { dependsOn(prismaGenerate) }

license {
    exclude("prismakt/generated/*.kt")
}

tasks.named<Test>("test") {
    finalizedBy(tasks.named<JacocoReport>("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named<Test>("test"))
    reports {
        xml.required = true
        html.required = true
    }
}
