import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("tegral.base-conventions")

    id("org.jetbrains.kotlin.jvm")
    id("io.gitlab.arturbosch.detekt")

    id("java-library")
    id("jacoco")
}

val libs = the<LibrariesForLibs>()

repositories {
    mavenCentral()
}

dependencies {
    api(libs.kotlin.stdlib)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.junit.jupiterApi)
    testImplementation(libs.logback)
    testRuntimeOnly(libs.junit.jupiterEngine)

    detektPlugins(libs.detekt.formatting)
}

testing {
    suites {
        // Configure the built-in test suite
        @Suppress("UnstableApiUsage")
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter()
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}
