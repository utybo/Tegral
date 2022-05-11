plugins {
    id 'org.jetbrains.kotlin.jvm'
    id 'io.gitlab.arturbosch.detekt'

    id 'java-library'
}

repositories {
    mavenCentral()
}

version = rootProject.version
group = rootProject.group

dependencies {
    implementation libs.kotlin.stdlib

    testImplementation libs.kotlin.test
    testImplementation libs.kotlin.test.junit5
    testImplementation libs.junit.jupiterApi
    testRuntimeOnly libs.junit.jupiterEngine

    detektPlugins libs.detekt.formatting
}

testing {
    suites {
        // Configure the built-in test suite
        test {
            // Use JUnit Jupiter test framework
            useJUnitJupiter()
        }
    }
}
