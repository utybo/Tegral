plugins {
    id("tegral.kotlin-common-conventions")
}

dependencies {
    implementation(project(":tegral-di:tegral-di-core"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}
