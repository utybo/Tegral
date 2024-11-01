plugins {
    id("jacoco")
}

repositories {
    mavenCentral()
}

val aggregatedProjects: Configuration by configurations.creating {
    isCanBeResolved = false
    isCanBeConsumed = false
}

val aggregatedProjectsExecData: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    isTransitive = false

    extendsFrom(aggregatedProjects)

    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.VERIFICATION))
        attribute(
            VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
            objects.named(VerificationType::class.java, VerificationType.JACOCO_RESULTS)
        )
    }
}

val aggregatedProjectsSources: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    isTransitive = false

    extendsFrom(aggregatedProjects)

    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling::class.java, Bundling.EXTERNAL))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.VERIFICATION))
        attribute(
            VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
            objects.named(VerificationType::class.java, VerificationType.MAIN_SOURCES)
        )
    }
}

val aggregatedProjectsClasses: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    isTransitive = false

    extendsFrom(aggregatedProjects)

    attributes {
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class.java, LibraryElements.CLASSES))
    }
}

val compositeReportExecData: Configuration by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    extendsFrom(aggregatedProjectsExecData)

    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.VERIFICATION))
        attribute(
            VerificationType.VERIFICATION_TYPE_ATTRIBUTE,
            objects.named(VerificationType::class.java, VerificationType.JACOCO_RESULTS)
        )
    }
}

tasks.register<JacocoReport>("aggregatedCodeCoverage") {
    executionData(aggregatedProjectsExecData)
    sourceDirectories.from(aggregatedProjectsSources)
    classDirectories.from(aggregatedProjectsClasses)

    reports {
        html.required = true
        xml.required = true
    }
}
