import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    id("jacoco-report-aggregation")
}

repositories {
    mavenCentral()
}

val testProjects by lazy {
    listOf(project(":tegral-prismakt:tegral-prismakt-generator")) +
        project(":tegral-prismakt:tegral-prismakt-generator-tests").subprojects
}

val inputExecutionData by lazy {
    testProjects.flatMap { it.tasks.withType<JacocoReport>().map { reportTask -> reportTask.executionData } }
}

val inputSourceSets by lazy {
    testProjects.flatMap { it.sourceSets }
}

val compositeCodeCoverage = tasks.register<JacocoReport>("compositeCodeCoverage") {
    executionData(inputExecutionData)
    sourceSets(*inputSourceSets.toTypedArray())
}

val compositeReportExecDataCfg = configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false

    artifacts {
        add("compositeReport", compositeCodeCoverage)
    }

    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.VERIFICATION))
        attribute(TestSuiteType.TEST_SUITE_TYPE_ATTRIBUTE, objects.named(TestSuiteType.UNIT_TEST))
        attribute(VerificationType.VERIFICATION_TYPE_ATTRIBUTE, objects.named(VerificationType.JACOCO_RESULTS))
        attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, ArtifactTypeDefinition.BINARY_DATA_TYPE)
    }
}

val compositeReportSrcCfg = configurations.creating {

}

// TODO set up input configurations with variants

/*
 * source code* ----> configA <-+
 *     classes* ----> configB   +- composeReportTask -> compositeReportCfg -> :code-coverage
 *    execdata  ----> configC <-+
 *
 *
 *     * See what the aggregate plugin uses for those
 */
